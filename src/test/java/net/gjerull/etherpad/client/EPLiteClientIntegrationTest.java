package net.gjerull.etherpad.client;

import java.util.*;

import org.junit.Before;
import org.junit.After;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import org.mockserver.model.HttpRequest;
import org.mockserver.model.HttpResponse;
import org.mockserver.integration.ClientAndServer;
import static org.mockserver.integration.ClientAndServer.startClientAndServer;
import org.mockserver.model.StringBody;

/**
 * Integration test for simple App.
 */
public class EPLiteClientIntegrationTest {
    private EPLiteClient client;
	private ClientAndServer mockServer;

    /**
     * Useless testing as it depends on a specific API key
     *
     * TODO: Find a way to make it configurable
     */
	@Before
	public void setUp() throws Exception {
		this.client = new EPLiteClient(
                "http://localhost:9001",
                "a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58"
        );
        this.mockServer = startClientAndServer(9001);
        
	}

	@After
	public void stopMockServer() {
		mockServer.stop();
	}

	@Test
	public void validate_token() throws Exception {

//		REQUEST, v1.2.13:checkToken, {"apikey":"a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58"}
//		RESPONSE, checkToken, {"code":0,"message":"ok","data":null}      

		mockServer.when(HttpRequest.request().withMethod("GET").withPath("/api/1.2.13/checkToken")
				.withBody("{\"apikey\":\"a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58\"}"))
				.respond(HttpResponse.response().withStatusCode(200)
				.withBody("{\"code\":0,\"message\":\"ok\",\"data\":null}"));

		client.checkToken();
	}

	@Test
	public void create_and_delete_group() throws Exception {

//		REQUEST, v1.2.13:createGroup, {"apikey":"a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58"}
//		RESPONSE, createGroup, {"code":0,"message":"ok","data":{"groupID":"g.ctm5zVmzeUSHLDwY"}}

		mockServer.when(HttpRequest.request().withMethod("POST").withPath("/api/1.2.13/createGroup").withBody(
            new StringBody("apikey=a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58")))
				.respond(HttpResponse.response().withStatusCode(200)
				.withBody("{\"code\":0,\"message\":\"ok\",\"data\":{\"groupID\":\"g.ctm5zVmzeUSHLDwY\"}}"));

		Map response = client.createGroup();

		assertTrue(response.containsKey("groupID"));
		String groupId = (String) response.get("groupID");
        assertTrue("Unexpected groupID " + groupId, groupId != null && groupId.startsWith("g."));

//		REQUEST, v1.2.13:deleteGroup, {"apikey":"a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58","groupID":"g.ctm5zVmzeUSHLDwY"}
//		RESPONSE, deleteGroup, {"code":0,"message":"ok","data":null}       
		mockServer.when(HttpRequest.request().withMethod("POST").withPath("/api/1.2.13/deleteGroup").withBody(
            new StringBody("apikey=a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58&groupID=g.ctm5zVmzeUSHLDwY")))
            .respond(HttpResponse.response().withStatusCode(200).withBody("{\"code\":0,\"message\":\"ok\",\"data\":null}"));

		client.deleteGroup(groupId);
	}

	@Test
	public void create_group_if_not_exists_for_and_list_all_groups() throws Exception {
        String groupMapper = "groupname";
//      REQUEST, v1.2.13:createGroupIfNotExistsFor, {"apikey":"a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58","groupMapper":"groupname"}
//      RESPONSE, createGroupIfNotExistsFor, {"code":0,"message":"ok","data":{"groupID":"g.2RjTUOQhoiBJhSox"}}

		mockServer.when(HttpRequest.request().withMethod("POST").withPath("/api/1.2.13/createGroupIfNotExistsFor"))
				.respond(HttpResponse.response().withStatusCode(200)
				.withBody("{\"code\":0,\"message\":\"ok\",\"data\":{\"groupID\":\"g.2RjTUOQhoiBJhSox\"}}"));

		Map response = client.createGroupIfNotExistsFor(groupMapper);

		assertTrue(response.containsKey("groupID"));
		String groupId = (String) response.get("groupID");

//      REQUEST, v1.2.13:listAllGroups, {"apikey":"a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58"}
//      RESPONSE, listAllGroups, {"code":0,"message":"ok","data":{"groupIDs":["g.2RjTUOQhoiBJhSox"]}}
		mockServer.when(HttpRequest.request().withMethod("GET").withPath("/api/1.2.13/listAllGroups"))
				.respond(HttpResponse.response().withStatusCode(200)
				.withBody("{\"code\":0,\"message\":\"ok\",\"data\":{\"groupIDs\":[\"g.2RjTUOQhoiBJhSox\"]}}"));

		try {
			Map listResponse = client.listAllGroups();
			assertTrue(listResponse.containsKey("groupIDs"));
			int firstNumGroups = ((List) listResponse.get("groupIDs")).size();

//      REQUEST, v1.2.13:createGroupIfNotExistsFor, {"apikey":"a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58","groupMapper":"groupname"}
//      RESPONSE, createGroupIfNotExistsFor, {"code":0,"message":"ok","data":{"groupID":"g.2RjTUOQhoiBJhSox"}}
			mockServer.when(HttpRequest.request().withMethod("POST").withPath("/api/1.2.13/createGroupIfNotExistsFor"))
					.respond(HttpResponse.response().withStatusCode(200)
					.withBody("{\"code\":0,\"message\":\"ok\",\"data\":{\"groupID\":\"g.2RjTUOQhoiBJhSox\"}}"));

			client.createGroupIfNotExistsFor(groupMapper);

//      REQUEST, v1.2.13:listAllGroups, {"apikey":"a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58"}
//      RESPONSE, listAllGroups, {"code":0,"message":"ok","data":{"groupIDs":["g.2RjTUOQhoiBJhSox"]}}
			mockServer.when(HttpRequest.request().withMethod("GET").withPath("/api/1.2.13/listAllGroups"))
					.respond(HttpResponse.response().withStatusCode(200)
					.withBody("{\"code\":0,\"message\":\"ok\",\"data\":{\"groupIDs\":[\"g.2RjTUOQhoiBJhSox\"]}}"));

			listResponse = client.listAllGroups();
			int secondNumGroups = ((List) listResponse.get("groupIDs")).size();

			assertEquals(firstNumGroups, secondNumGroups);
		} finally {

//      REQUEST, v1.2.13:deleteGroup, {"apikey":"a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58","groupID":"g.2RjTUOQhoiBJhSox"}
//      RESPONSE, deleteGroup, {"code":0,"message":"ok","data":null}

            mockServer.when(HttpRequest.request().withMethod("POST").withPath("/api/1.2.13/deleteGroup"))
					.respond(HttpResponse.response().withStatusCode(200)
					.withBody("{\"code\":0,\"message\":\"ok\",\"data\":null}"));

			client.deleteGroup(groupId);
		}
	}

	@Test
	 public void create_group_pads_and_list_them() throws Exception {

	    mockServer.when(HttpRequest.request().withMethod("POST").withPath("/api/1.2.13/createGroup"))
	        .respond(HttpResponse.response().withStatusCode(200)
            .withBody("{\"code\":0,\"message\":\"ok\",\"data\":{\"groupID\":\"g.12\"}}"));
     
        Map response = client.createGroup();
     
	    mockServer.when(HttpRequest.request().withMethod("POST").withPath("/api/1.2.13/createGroupPad"))
	        .respond(HttpResponse.response().withStatusCode(200)
            .withBody("{\"code\":0,\"message\":\"ok\",\"data\":{\"padID\":\"g.12$integration-test-1\"}}"));
     
	    String groupId = (String) response.get("groupID");
	    String pad1 = "integration-test-1";
        String pad2 = "integration-test-2";
     
	    try {

	        Map padResponse = client.createGroupPad(groupId, pad1);
	        assertTrue(padResponse.containsKey("padID"));
	        String padId1 = (String) padResponse.get("padID");
	
	        mockServer.when(HttpRequest.request().withMethod("POST").withPath("/api/1.2.13/setPublicStatus"))
	            .respond(HttpResponse.response().withStatusCode(200)
                .withBody("{\"code\":0,\"message\":\"ok\",\"data\":null}"));
        
	        mockServer.when(HttpRequest.request().withMethod("GET").withPath("/api/1.2.13/getPublicStatus"))
	            .respond(HttpResponse.response().withStatusCode(200)
                .withBody("{\"code\":0,\"message\":\"ok\",\"data\":{\"publicStatus\":true}}"));
        
	        client.setPublicStatus(padId1, true);
	        boolean publicStatus = (boolean)
	        client.getPublicStatus(padId1).get("publicStatus");
	        assertTrue(publicStatus);
	
	        mockServer.when(HttpRequest.request().withMethod("POST").withPath("/api/1.2.13/setPassword"))
	            .respond(HttpResponse.response().withStatusCode(200)
                .withBody("{\"code\":0,\"message\":\"ok\",\"data\":null}"));
        
	        mockServer.when(HttpRequest.request().withMethod("GET").withPath("/api/1.2.13/isPasswordProtected"))
	            .respond(HttpResponse.response().withStatusCode(200)
                .withBody("{\"code\":0,\"message\":\"ok\",\"data\":{\"isPasswordProtected\":true}}"));
        
	        client.setPassword(padId1, "integration");
	        boolean passwordProtected = (boolean)
	        client.isPasswordProtected(padId1).get("isPasswordProtected");
	        assertTrue(passwordProtected);
	
	        mockServer.when(HttpRequest.request().withMethod("POST").withPath("/api/1.2.13/createGroupPad"))
	            .respond(HttpResponse.response().withStatusCode(200).withBody(
	            "{\"code\":0,\"message\":\"ok\",\"data\":{\"padID\":\"g.12$integration-test-2\"}}"));
	
	        padResponse = client.createGroupPad(groupId, pad2, "Initial text");
	        assertTrue(padResponse.containsKey("padID"));
	
	        mockServer.when(HttpRequest.request().withMethod("GET").withPath("/api/1.2.13/getText"))
	            .respond(HttpResponse.response().withStatusCode(200)
		        .withBody("{\"code\":0,\"message\":\"ok\",\"data\":{\"text\":\"Initial text\\n\"}}"));
	
	        String padId = (String) padResponse.get("padID");
	        String initialText = (String) client.getText(padId).get("text");
	        assertEquals("Initial text\n", initialText);
	
	        mockServer.when(HttpRequest.request().withMethod("GET").withPath("/api/1.2.13/listPads"))
	            .respond(HttpResponse.response().withStatusCode(200).withBody(
	            "{\"code\":0,\"message\":\"ok\",\"data\":{\"padIDs\":[\"g.12$integration-test-1\",\"g.12$integration-test-2\"]}}"));
	
	        Map padListResponse = client.listPads(groupId);
	        assertTrue(padListResponse.containsKey("padIDs"));
	        List padIds = (List) padListResponse.get("padIDs");
            assertEquals(2, padIds.size());
        
	    } finally {

	        mockServer.when(HttpRequest.request().withMethod("POST").withPath("/api/1.2.13/deleteGroup"))
                .respond(HttpResponse.response().withStatusCode(200)
                .withBody("{\"code\":0,\"message\":\"ok\",\"data\":null}"));
        
	        client.deleteGroup(groupId);
	    }
	}

	@Test
	public void create_author() throws Exception {

//      REQUEST, v1.2.13:createAuthor, {"apikey":"a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58"}
//      RESPONSE, createAuthor, {"code":0,"message":"ok","data":{"authorID":"a.4Bpj0MOtz5WLJvsH"}}

		mockServer.when(HttpRequest.request().withMethod("GET").withPath("/api/1.2.13/createAuthor"))
				.respond(HttpResponse.response().withStatusCode(200)
                        .withBody("{\"code\":0,\"message\":\"ok\",\"data\":{\"authorID\":\"a.4Bpj0MOtz5WLJvsH\"}}"));
                        
		Map authorResponse = client.createAuthor();
		String authorId = (String) authorResponse.get("authorID");
        assertTrue(authorId != null && !authorId.isEmpty());

//      REQUEST, v1.2.13:createAuthor, {"apikey":"a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58","name":"integration-author"}
//      RESPONSE, createAuthor, {"code":0,"message":"ok","data":{"authorID":"a.1gZuM2urPfqBrr0H"}}

		mockServer.when(HttpRequest.request().withMethod("POST").withPath("/api/1.2.13/createAuthor"))
				.respond(HttpResponse.response().withStatusCode(200)
				.withBody("{\"code\":0,\"message\":\"ok\",\"data\":{\"authorID\":\"a.1gZuM2urPfqBrr0H\"}}"));

		authorResponse = client.createAuthor("integration-author");
        authorId = (String) authorResponse.get("authorID");
        
//      REQUEST, v1.2.13:getAuthorName, {"apikey":"a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58","authorID":"a.1gZuM2urPfqBrr0H"}
//      RESPONSE, getAuthorName, {"code":0,"message":"ok","data":"integration-author"}

		mockServer.when(HttpRequest.request().withMethod("GET").withPath("/api/1.2.13/getAuthorName"))
				.respond(HttpResponse.response().withStatusCode(200)
						.withBody("{\"code\":0,\"message\":\"ok\",\"data\":\"integration-author\"}"));

		String authorName = client.getAuthorName(authorId);
		assertEquals("integration-author", authorName);
	}

	@Test
	public void create_author_with_author_mapper() throws Exception {

        String authorMapper = "username";
        
		mockServer.when(HttpRequest.request().withMethod("POST").withPath("/api/1.2.13/createAuthorIfNotExistsFor"))
				.respond(HttpResponse.response().withStatusCode(200)
				.withBody("{\"code\":0,\"message\":\"ok\",\"data\":{\"authorID\":\"a.01\"}}"));

		Map authorResponse = client.createAuthorIfNotExistsFor(authorMapper, "integration-author-1");
		String Author1Id = (String) authorResponse.get("authorID");
        assertTrue(Author1Id != null && !Author1Id.isEmpty());
        
		mockServer.when(HttpRequest.request().withMethod("GET").withPath("/api/1.2.13/getAuthorName"))
				.respond(HttpResponse.response().withStatusCode(200)
                .withBody("{\"code\":0,\"message\":\"ok\",\"data\":\"integration-author-1\"}"));
                        
		String Author1Name = client.getAuthorName(Author1Id);

		mockServer.clear(HttpRequest.request().withMethod("GET").withPath("/api/1.2.13/getAuthorName"));

		mockServer.when(HttpRequest.request().withMethod("POST").withPath("/api/1.2.13/createAuthorIfNotExistsFor"))
				.respond(HttpResponse.response().withStatusCode(200)
				.withBody("{\"code\":0,\"message\":\"ok\",\"data\":{\"authorID\":\"a.01\"}}"));

		authorResponse = client.createAuthorIfNotExistsFor(authorMapper, "integration-author-2");
		String Author2Id = (String) authorResponse.get("authorID");
		assertEquals(Author1Id, Author2Id);

		mockServer.when(HttpRequest.request().withMethod("GET").withPath("/api/1.2.13/getAuthorName"))
				.respond(HttpResponse.response().withStatusCode(200)
                .withBody("{\"code\":0,\"message\":\"ok\",\"data\":\"integration-author-2\"}"));
                        
		String Author2Name = client.getAuthorName(Author2Id);
        assertNotEquals(Author1Name, Author2Name);
        
		mockServer.when(HttpRequest.request().withMethod("POST").withPath("/api/1.2.13/createAuthorIfNotExistsFor"))
				.respond(HttpResponse.response().withStatusCode(200)
                .withBody("{\"code\":0,\"message\":\"ok\",\"data\":{\"authorID\":\"a.01\"}}"));
                        
		mockServer.when(HttpRequest.request().withMethod("GET").withPath("/api/1.2.13/getAuthorName"))
				.respond(HttpResponse.response().withStatusCode(200)
                .withBody("{\"code\":0,\"message\":\"ok\",\"data\":\"integration-author-2\"}"));
                        
		authorResponse = client.createAuthorIfNotExistsFor(authorMapper);
		String Author3Id = (String) authorResponse.get("authorID");
		assertEquals(Author2Id, Author3Id);
		String Author3Name = client.getAuthorName(Author3Id);
		assertEquals(Author2Name, Author3Name);
	}
}