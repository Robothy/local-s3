package com.robothy.s3.datatypes.serializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.robothy.s3.datatypes.Grantee;
import java.io.IOException;

public class GranteeSerializer extends StdSerializer<Grantee> {

  public GranteeSerializer(Class<Grantee> t) {
    super(t);
  }

  @Override
  public void serialize(Grantee value, JsonGenerator gen, SerializerProvider provider) throws IOException {
    gen.writeStartObject();
    gen.writeObject(value.getDisplayName());
    gen.writeObject(value.getEmailAddress());
    gen.writeObject(value.getId());
    gen.writeObject(value.getUri());
    gen.writeStringField("type", value.getType());
    gen.writeEndObject();
  }
}
