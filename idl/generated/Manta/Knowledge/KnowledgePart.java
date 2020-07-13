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

package Manta.Knowledge;

public class KnowledgePart extends com.zeroc.Ice.Value
{
    public KnowledgePart()
    {
        this.value = "";
    }

    public KnowledgePart(String value)
    {
        this.value = value;
    }

    public String value;

    public KnowledgePart clone()
    {
        return (KnowledgePart)super.clone();
    }

    public static String ice_staticId()
    {
        return "::Manta::Knowledge::KnowledgePart";
    }

    @Override
    public String ice_id()
    {
        return ice_staticId();
    }

    /** @hidden */
    public static final long serialVersionUID = -832469328846591523L;

    /** @hidden */
    @Override
    protected void _iceWriteImpl(com.zeroc.Ice.OutputStream ostr_)
    {
        ostr_.startSlice(ice_staticId(), -1, true);
        ostr_.writeString(value);
        ostr_.endSlice();
    }

    /** @hidden */
    @Override
    protected void _iceReadImpl(com.zeroc.Ice.InputStream istr_)
    {
        istr_.startSlice();
        value = istr_.readString();
        istr_.endSlice();
    }
}
