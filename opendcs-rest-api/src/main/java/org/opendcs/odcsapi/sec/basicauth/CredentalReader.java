package org.opendcs.odcsapi.sec.basicauth;

import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.Provider;

@Provider
public class CredentalReader implements MessageBodyReader<Credentials>
{

@Override
public boolean isReadable(Class<?> paramClass, Type paramType,
        Annotation[] paramArrayOfAnnotation, MediaType mediaType) {
            return paramType == Credentials.class && mediaType.isCompatible(MediaType.APPLICATION_FORM_URLENCODED_TYPE);
}

@Override
public Credentials readFrom(Class<Credentials> type, Type genericType, Annotation[] annotations, MediaType mediaType,
        MultivaluedMap<String, String> httpHeaders, InputStream entityStream)
        throws IOException, WebApplicationException {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'readFrom'");
}


}