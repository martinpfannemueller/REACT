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

package Manta.MAPECommunication;

public class EffectingKnowledgeRecord extends KnowledgeRecord
{
    public EffectingKnowledgeRecord()
    {
        super();
        this.components = new Manta.Effecting.ComponentChange();
    }

    public EffectingKnowledgeRecord(String type, String category, String ownerID, long timeStamp, Manta.Effecting.ComponentChange components, Manta.Effecting.Parameter[] parameterChanges)
    {
        super(type, category, ownerID, timeStamp);
        this.components = components;
        this.parameterChanges = parameterChanges;
    }

    public Manta.Effecting.ComponentChange components;

    public Manta.Effecting.Parameter[] parameterChanges;

    public EffectingKnowledgeRecord clone()
    {
        return (EffectingKnowledgeRecord)super.clone();
    }

    public static String ice_staticId()
    {
        return "::Manta::MAPECommunication::EffectingKnowledgeRecord";
    }

    @Override
    public String ice_id()
    {
        return ice_staticId();
    }

    /** @hidden */
    public static final long serialVersionUID = 6855790481503776902L;

    /** @hidden */
    @Override
    protected void _iceWriteImpl(com.zeroc.Ice.OutputStream ostr_)
    {
        ostr_.startSlice(ice_staticId(), -1, false);
        Manta.Effecting.ComponentChange.ice_write(ostr_, components);
        ParameterChangesHelper.write(ostr_, parameterChanges);
        ostr_.endSlice();
        super._iceWriteImpl(ostr_);
    }

    /** @hidden */
    @Override
    protected void _iceReadImpl(com.zeroc.Ice.InputStream istr_)
    {
        istr_.startSlice();
        components = Manta.Effecting.ComponentChange.ice_read(istr_);
        parameterChanges = ParameterChangesHelper.read(istr_);
        istr_.endSlice();
        super._iceReadImpl(istr_);
    }
}
