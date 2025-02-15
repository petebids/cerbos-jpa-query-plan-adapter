package com.github.petebids;


import com.google.protobuf.Value;
import dev.cerbos.api.v1.engine.Engine.PlanResourcesFilter.Expression.Operand;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Root;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.Assert;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.springframework.data.jpa.domain.Specification.allOf;
import static org.springframework.data.jpa.domain.Specification.anyOf;
import static org.springframework.data.jpa.domain.Specification.not;


public class QueryPlanAdapter<T> {

    public static final String REQUEST_RESOURCE_ATTR = "request.resource.attr";
    public static final String REPLACEMENT = "";
    public static final String REGEX = "\\.";
    private final Map<String, AttributeTarget> propertyMappings;

    public QueryPlanAdapter(Map<String, AttributeTarget> propertyMappings) {
        this.propertyMappings = propertyMappings;
    }

    public QueryPlanAdapter() {
        this(Collections.emptyMap());
    }

    public Specification<T> adapt(Operand operand) {

        return switch (SupportedOperators.valueOf(operand.getExpression().getOperator().toUpperCase())) {

            case AND -> allOf(operand
                    .getExpression()
                    .getOperandsList()
                    .stream()
                    .map(this::adapt)
                    .toList());
            case OR -> anyOf(operand
                    .getExpression()
                    .getOperandsList()
                    .stream()
                    .map(this::adapt)
                    .toList());
            case NOT -> not(allOf(operand
                    .getExpression()
                    .getOperandsList()
                    .stream()
                    .map(this::adapt)
                    .toList()));
            case EQ -> (root, query, criteriaBuilder) -> {
                final OperandPair pair = parseInfixOrFail(operand);
                return criteriaBuilder.equal(
                        toExpression(pair.left(), pair.right(), root, criteriaBuilder),
                        toExpression(pair.right(), pair.left(), root, criteriaBuilder)
                );
            };
            case IN -> (root, query, criteriaBuilder) -> {
                final OperandPair pair = parseInfixOrFail(operand);
                return getObjectPath(pair.left(), root).in
                        (pair.right().getValue().getListValue().getValuesList().stream().map(Value::getStringValue).toList());
            };
            case NE -> (root, query, criteriaBuilder) -> {
                final OperandPair pair = parseInfixOrFail(operand);
                return criteriaBuilder.notEqual(
                        toExpression(pair.left(), pair.right(), root, criteriaBuilder),
                        toExpression(pair.right(), pair.left(), root, criteriaBuilder));
            };

            case LE -> (root, query, criteriaBuilder) -> {
                final OperandPair pair = parseInfixOrFail(operand);
                return criteriaBuilder.lessThanOrEqualTo(
                        getObjectPath(pair.left(), root).as(Double.class),
                        pair.right().getValue().getNumberValue()
                );

            };
            case LT -> (root, query, criteriaBuilder) -> {
                final OperandPair pair = parseInfixOrFail(operand);
                return criteriaBuilder.lessThan(getObjectPath(pair.left(), root).as(Double.class),
                        pair.right().getValue().getNumberValue());
            };
            case GT -> (root, query, criteriaBuilder) -> {
                final OperandPair pair = parseInfixOrFail(operand);
                return criteriaBuilder.greaterThan(getObjectPath(pair.left(), root).as(Double.class),
                        pair.right().getValue().getNumberValue());
            };
            case GTE -> (root, query, criteriaBuilder) -> {
                final OperandPair pair = parseInfixOrFail(operand);
                return criteriaBuilder.greaterThanOrEqualTo(getObjectPath(pair.left(), root).as(Double.class),
                        pair.right().getValue().getNumberValue());
            };


        };

    }


    private Expression<?> toExpression(Operand operand,
                                       Operand other,
                                       Root<T> root,
                                       CriteriaBuilder criteriaBuilder) {
        if (operand.hasVariable()) {
            return getObjectPath(operand, root);
        }

        if (operand.hasValue()) {
            // use the other operand to find the type of this one
            final Class<?> clazz = getClazz(other, root);
            if (clazz == Boolean.class) {
                return criteriaBuilder.literal(operand.getValue().getBoolValue());
            } else if (clazz == Integer.class) {
                return criteriaBuilder.literal(operand.getValue().getNumberValue());
            } else if (clazz == UUID.class) {
                return criteriaBuilder.literal(UUID.fromString(operand.getValue().getStringValue()));
            }
            // String and null mapping
            else {
                return criteriaBuilder.literal(operand.getValue().getStringValue());
            }
        }
        throw new IllegalStateException("operand must have a variable or value");

    }


    private Class<?> getClazz(Operand other, Root<T> root) {
        final AttributeTarget attributeTarget = propertyMappings.get(other.getVariable());
        if (attributeTarget != null) {
            return attributeTarget.clazz();
        }
        final Path<Object> objectPath = getObjectPath(other, root);
        if (objectPath == null) {
            throw new NullPointerException();
        }
        return objectPath.getJavaType();
    }

    private Path<Object> getObjectPath(Operand operand, Root<T> root) {
        final AttributeTarget attributeTarget = propertyMappings.get(operand.getVariable());

        String pathToUse;

        // If an override is not supplied by the user, use the path as is from the policy as a JPA path
        if (attributeTarget == null || attributeTarget.jpaPath() == null) {
            pathToUse = operand.getVariable();
        } else {
            pathToUse = attributeTarget.jpaPath();
        }
        Path<Object> path = null;
        for (String chunk : pathToUse
                .replaceFirst(REQUEST_RESOURCE_ATTR, REPLACEMENT)
                .split(REGEX)) {
            if (chunk.isBlank()) {
                continue;
            }
            path = root.get(chunk);
        }
        return path;
    }

    private OperandPair parseInfixOrFail(Operand operand) {
        final List<Operand> operands = operand.getExpression().getOperandsList();
        Assert.isTrue(operands.size() == 2, "Expected two operands for operator : " + operand.getExpression().getOperator());
        return new OperandPair(operands.get(0), operands.get(1));

    }

}


record OperandPair(Operand left, Operand right) {
}