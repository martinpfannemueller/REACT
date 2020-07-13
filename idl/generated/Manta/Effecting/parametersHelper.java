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

/**
 * Helper class for marshaling/unmarshaling parameters.
 **/
public final class parametersHelper
{
    public static void write(com.zeroc.Ice.OutputStream ostr, Parameter[] v)
    {
        if(v == null)
        {
            ostr.writeSize(0);
        }
        else
        {
            ostr.writeSize(v.length);
            for(int i0 = 0; i0 < v.length; i0++)
            {
                Parameter.ice_write(ostr, v[i0]);
            }
        }
    }

    public static Parameter[] read(com.zeroc.Ice.InputStream istr)
    {
        final Parameter[] v;
        final int len0 = istr.readAndCheckSeqSize(2);
        v = new Parameter[len0];
        for(int i0 = 0; i0 < len0; i0++)
        {
            v[i0] = Parameter.ice_read(istr);
        }
        return v;
    }

    public static void write(com.zeroc.Ice.OutputStream ostr, int tag, java.util.Optional<Parameter[]> v)
    {
        if(v != null && v.isPresent())
        {
            write(ostr, tag, v.get());
        }
    }

    public static void write(com.zeroc.Ice.OutputStream ostr, int tag, Parameter[] v)
    {
        if(ostr.writeOptional(tag, com.zeroc.Ice.OptionalFormat.FSize))
        {
            int pos = ostr.startSize();
            parametersHelper.write(ostr, v);
            ostr.endSize(pos);
        }
    }

    public static java.util.Optional<Parameter[]> read(com.zeroc.Ice.InputStream istr, int tag)
    {
        if(istr.readOptional(tag, com.zeroc.Ice.OptionalFormat.FSize))
        {
            istr.skip(4);
            Parameter[] v;
            v = parametersHelper.read(istr);
            return java.util.Optional.of(v);
        }
        else
        {
            return java.util.Optional.empty();
        }
    }
}
