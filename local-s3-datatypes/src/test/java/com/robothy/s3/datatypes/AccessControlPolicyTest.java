package com.robothy.s3.datatypes;

import static org.junit.jupiter.api.Assertions.assertEquals;
import com.ctc.wstx.stax.WstxInputFactory;
import com.ctc.wstx.stax.WstxOutputFactory;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.dataformat.xml.XmlFactory;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import java.util.List;
import java.util.stream.Collectors;
import javax.xml.stream.XMLInputFactory;
import org.junit.jupiter.api.Test;

class AccessControlPolicyTest {

  @Test
  void serialization() throws JsonProcessingException {

    AccessControlPolicy accessControlPolicy = new AccessControlPolicy();

    Owner owner = new Owner();
    owner.setId("123");
    owner.setDisplayName("Robothy");
    accessControlPolicy.setOwner(owner);

    Grant grant1 = new Grant();
    Grantee grantee1 = new Grantee();
    grantee1.setEmailAddress("abc@123.com");
    grantee1.setId("666");
    grantee1.setDisplayName("ABC");
    grant1.setGrantee(grantee1);
    grant1.setPermission("READ");

    Grant grant2 = new Grant();
    Grantee grantee2 = new Grantee();
    grantee2.setUri("http://localhost/Hello");
    grantee2.setType("CanonicalUser ");
    grant2.setGrantee(grantee2);
    grant2.setPermission("WRITE");

    accessControlPolicy.setGrants(List.of(grant1, grant2));

    XMLInputFactory input = new WstxInputFactory();
    input.setProperty(XMLInputFactory.IS_NAMESPACE_AWARE, Boolean.FALSE);
    XmlMapper xmlMapper = new XmlMapper(new XmlFactory(input, new WstxOutputFactory()));


    String xml = xmlMapper.writerWithDefaultPrettyPrinter()
        .writeValueAsString(accessControlPolicy);

    assertEquals(accessControlPolicy, xmlMapper.readValue(xml, AccessControlPolicy.class));
  }

  @Test
  void deserialization() throws JsonProcessingException {
    String xml = """
        <AccessControlPolicy xmlns="http://s3.amazonaws.com/doc/2006-03-01/">
        	<Owner>
        		<ID>001</ID>
        		<DisplayName>LocalS3</DisplayName>
        	</Owner>
        	
        	<AccessControlList>
        		<Grant>
        			<Grantee xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:type="CanonicalUser">
        				<ID>123</ID>
        			</Grantee>
        			<Permission>FULL_CONTROL</Permission>
        		</Grant>
        		<Grant>
        			<Grantee xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:type="CanonicalUser">
        				<ID>001</ID>
        			</Grantee>
        			<Permission>FULL_CONTROL</Permission>
        		</Grant>
        		<Grant>
        			<Grantee xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:type="Group">
        				<URI>http://acs.amazonaws.com/groups/global/AllUsers</URI>
        			</Grantee>
        			<Permission>READ</Permission>
        		</Grant>
        	</AccessControlList>
        </AccessControlPolicy>
        """;

    XmlMapper xmlMapper = new XmlMapper();
    AccessControlPolicy acl = xmlMapper.readValue(xml, AccessControlPolicy.class);
    Owner owner = acl.getOwner();
    assertEquals("001", owner.getId());
    assertEquals("LocalS3", owner.getDisplayName());

    List<Grant> grants = acl.getGrants();
    assertEquals(3, grants.size());
    List<Grant> groupTypeGrants =
        grants.stream().filter(grant -> "Group".equals(grant.getGrantee().getType())).collect(Collectors.toList());
    assertEquals(1, groupTypeGrants.size());
    assertEquals("READ", groupTypeGrants.get(0).getPermission());
    assertEquals("http://acs.amazonaws.com/groups/global/AllUsers", groupTypeGrants.get(0).getGrantee().getUri());
  }

}