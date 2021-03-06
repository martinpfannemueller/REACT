//
// Copyright (c) ZeroC, Inc. All rights reserved.
//
//
// Ice version 3.7.2
//
// <auto-generated>
//
// Generated from file `Manta.ice'
//
// Warning: do not edit this file.
//
// </auto-generated>
//

package Manta.Effecting;

public class ParameterChange implements java.lang.Cloneable,
                                        java.io.Serializable
{
    public Parameter parameter;

    public ParameterChange()
    {
        this.parameter = new Parameter();
    }

    public ParameterChange(Parameter parameter)
    {
        this.parameter = parameter;
    }

    public boolean equals(java.lang.Object rhs)
    {
        if(this == rhs)
        {
            return true;
        }
        ParameterChange r = null;
        if(rhs instanceof ParameterChange)
        {
            r = (ParameterChange)rhs;
        }

        if(r != null)
        {
            if(this.parameter != r.parameter)
            {
                if(this.parameter == null || r.parameter == null || !this.parameter.equals(r.parameter))
                {
                    return false;
                }
            }

            return true;
        }

        return false;
    }

    public int hashCode()
    {
        int h_ = 5381;
        h_ = com.zeroc.IceInternal.HashUtil.hashAdd(h_, "::Manta::Effecting::ParameterChange");
        h_ = com.zeroc.IceInternal.HashUtil.hashAdd(h_, parameter);
        return h_;
    }

    public ParameterChange clone()
    {
        ParameterChange c = null;
        try
        {
            c = (ParameterChange)super.clone();
        }
        catch(CloneNotSupportedException ex)
        {
            assert false; // impossible
        }
        return c;
    }

    public void ice_writeMembers(com.zeroc.Ice.OutputStream ostr)
    {
        Parameter.ice_write(ostr, this.parameter);
    }

    public void ice_readMembers(com.zeroc.Ice.InputStream istr)
    {
        this.parameter = Parameter.ice_read(istr);
    }

    static public void ice_write(com.zeroc.Ice.OutputStream ostr, ParameterChange v)
    {
        if(v == null)
        {
            _nullMarshalValue.ice_writeMembers(ostr);
        }
        else
        {
            v.ice_writeMembers(ostr);
        }
    }

    static public ParameterChange ice_read(com.zeroc.Ice.InputStream istr)
    {
        ParameterChange v = new ParameterChange();
        v.ice_readMembers(istr);
        return v;
    }

    static public void ice_write(com.zeroc.Ice.OutputStream ostr, int tag, java.util.Optional<ParameterChange> v)
    {
        if(v != null && v.isPresent())
        {
            ice_write(ostr, tag, v.get());
        }
    }

    static public void ice_write(com.zeroc.Ice.OutputStream ostr, int tag, ParameterChange v)
    {
        if(ostr.writeOptional(tag, com.zeroc.Ice.OptionalFormat.FSize))
        {
            int pos = ostr.startSize();
            ice_write(ostr, v);
            ostr.endSize(pos);
        }
    }

    static public java.util.Optional<ParameterChange> ice_read(com.zeroc.Ice.InputStream istr, int tag)
    {
        if(istr.readOptional(tag, com.zeroc.Ice.OptionalFormat.FSize))
        {
            istr.skip(4);
            return java.util.Optional.of(ParameterChange.ice_read(istr));
        }
        else
        {
            return java.util.Optional.empty();
        }
    }

    private static final ParameterChange _nullMarshalValue = new ParameterChange();

    /** @hidden */
    public static final long serialVersionUID = -6250416486435486454L;
}
