//
// Copyright (c) ZeroC, Inc. All rights reserved.
//
//
// Ice version 3.7.3
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

public class PIMKnowledge extends KnowledgePart
{
    public PIMKnowledge()
    {
        super();
    }

    public PIMKnowledge(String value)
    {
        super(value);
    }

    public PIMKnowledge clone()
    {
        return (PIMKnowledge)super.clone();
    }

    public static String ice_staticId()
    {
        return "::Manta::Knowledge::PIMKnowledge";
    }

    @Override
    public String ice_id()
    {
        return ice_staticId();
    }

    /** @hidden */
    public static final long serialVersionUID = 3702549537063796958L;

    /** @hidden */
    @Override
    protected void _iceWriteImpl(com.zeroc.Ice.OutputStream ostr_)
    {
        ostr_.startSlice(ice_staticId(), -1, false);
        ostr_.endSlice();
        super._iceWriteImpl(ostr_);
    }

    /** @hidden */
    @Override
    protected void _iceReadImpl(com.zeroc.Ice.InputStream istr_)
    {
        istr_.startSlice();
        istr_.endSlice();
        super._iceReadImpl(istr_);
    }
}
