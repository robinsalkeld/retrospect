/*******************************************************************************
 * Copyright (c) 2008, 2010 SAP AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SAP AG - initial API and implementation
 *******************************************************************************/
package org.eclipse.mat.parser.internal.oql.compiler;

import java.util.Arrays;
import java.util.List;

import org.eclipse.mat.parser.internal.oql.ICompiler;

public class CompilerImpl implements ICompiler
{

    public Object and(Object[] arguments)
    {
        return new Operation.And(Arrays.asList(arguments).toArray(new Expression[arguments.length]));
    }

    public Object or(Object[] arguments)
    {
        return new Operation.Or(Arrays.asList(arguments).toArray(new Expression[arguments.length]));
    }

    public Object equal(Object left, Object right)
    {
        return new Operation.Equal((Expression) left, (Expression) right);
    }

    public Object notEqual(Object left, Object right)
    {
        return new Operation.NotEqual((Expression) left, (Expression) right);
    }

    public Object greaterThan(Object left, Object right)
    {
        return new Operation.GreaterThan((Expression) left, (Expression) right);
    }

    public Object greaterThanOrEqual(Object left, Object right)
    {
        return new Operation.GreaterThanOrEqual((Expression) left, (Expression) right);
    }

    public Object lessThan(Object left, Object right)
    {
        return new Operation.LessThan((Expression) left, (Expression) right);
    }

    public Object lessThanOrEqual(Object left, Object right)
    {
        return new Operation.LessThanOrEqual((Expression) left, (Expression) right);
    }

    public Object like(Object ex, String regex)
    {
        return new Operation.Like((Expression) ex, regex);
    }

    public Object notLike(Object ex, String regex)
    {
        return new Operation.NotLike((Expression) ex, regex);
    }

    public Object in(Object left, Object right)
    {
        return new Operation.In((Expression) left, (Expression) right);
    }

    public Object notIn(Object left, Object right)
    {
        return new Operation.NotIn((Expression) left, (Expression) right);
    }

    public Object instanceOf(Object left, String className)
    {
        return new Operation.InstanceOf((Expression) left, className);
    }

    public Object literal(Object object)
    {
        return new ConstantExpression(object);
    }

    public Object nullLiteral()
    {
        return new ConstantExpression(ConstantExpression.NULL);
    }

    public Object path(List<Object> attributes)
    {
        return new PathExpression(attributes);
    }

    public Object method(String name, List<Expression> parameters, boolean isFirstInPath)
    {
        if (isFirstInPath && parameters.size() == 1)
        {
            Function f = function(name, parameters.get(0));
            if (f != null)
                return f;
        }

        return new MethodCallExpression(name, parameters);
    }

    public Function function(String name, Object subject)
    {
        if ("toHex".equals(name))//$NON-NLS-1$
        {
            return new Function.ToHex((Expression) subject);
        }
        else if ("toString".equals(name))//$NON-NLS-1$
        {
            return new Function.ToString((Expression) subject);
        }
        else if ("inbounds".equals(name))//$NON-NLS-1$
        {
            return new Function.Inbounds((Expression) subject);
        }
        else if ("outbounds".equals(name))//$NON-NLS-1$
        {
            return new Function.Outbounds((Expression) subject);
        }
        else if ("dominators".equals(name))//$NON-NLS-1$
        {
            return new Function.Dominators((Expression) subject);
        }
        else if ("classof".equals(name))//$NON-NLS-1$
        {
            return new Function.ClassOf((Expression) subject);
        }
        else if ("dominatorof".equals(name))//$NON-NLS-1$
        {
            return new Function.DominatorOf((Expression) subject);
        }
        else
        {
            return null;
        }
    }

    public Object subQuery(Query q)
    {
        return new QueryExpression(q);
    }

    public Object divide(Object left, Object right)
    {
        return new Operation.Divide((Expression) left, (Expression) right);
    }

    public Object minus(Object left, Object right)
    {
        return new Operation.Minus((Expression) left, (Expression) right);
    }

    public Object multiply(Object left, Object right)
    {
        return new Operation.Multiply((Expression) left, (Expression) right);
    }

    public Object plus(Object left, Object right)
    {
        return new Operation.Plus((Expression) left, (Expression) right);
    }

    // //////////////////////////////////////////////////////////////
    // helper classes
    // //////////////////////////////////////////////////////////////

    static class ConstantExpression extends Expression
    {
        public static final Object NULL = new Object();

        Object literal;

        public ConstantExpression(Object literal)
        {
            this.literal = literal;
        }

        @Override
        public Object compute(EvaluationContext ctx)
        {
            return literal;
        }

        @Override
        public boolean isContextDependent(EvaluationContext ctx)
        {
            return false;
        }

        @Override
        public String toString()
        {
            if (literal instanceof String)
                return "\"" + literal + "\"";//$NON-NLS-1$//$NON-NLS-2$
            else if (literal instanceof Character)
                return "'" + literal + "'";//$NON-NLS-1$//$NON-NLS-2$
            else if (literal instanceof Integer)
                return literal.toString();
            else if (literal instanceof Long)
                return literal + "L";//$NON-NLS-1$
            else if (literal == NULL)
                return "null";//$NON-NLS-1$
            else
                return String.valueOf(literal);
        }
    }

}
