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

	@Test
	public void create_pad_move_and_copy() throws Exception {
		
		String keep = "keep";
		String change = "change";
		String padID = "integration-test-pad";
		String copyPadId = "integration-test-pad-copy";
		String movePadId = "integration-test-pad-move";
	
		mockServer.when(HttpRequest.request().withMethod("POST").withPath("/api/1.2.13/createPad")).respond(
			HttpResponse.response().withStatusCode(200).withBody("{\"code\":0,\"message\":\"ok\",\"data\":null}"));
	
		client.createPad(padID, keep);
   
		mockServer.when(HttpRequest.request().withMethod("POST").withPath("/api/1.2.13/copyPad"))
			.respond(HttpResponse.response().withStatusCode(200).withBody(
			"{\"code\":0,\"message\":\"ok\",\"data\":{\"padID\":\"integration-test-pad-copy\"}}"));

		client.copyPad(padID, copyPadId);

		mockServer.when(HttpRequest.request().withMethod("GET").withPath("/api/1.2.13/getText"))
			.respond(HttpResponse.response().withStatusCode(200)
			.withBody("{\"code\":0,\"message\":\"ok\",\"data\":{\"text\":\"keep\\n\"}}"));

		String copyPadText = (String) client.getText(copyPadId).get("text");
	
		mockServer.when(HttpRequest.request().withMethod("POST").withPath("/api/1.2.13/movePad")).respond(
			HttpResponse.response().withStatusCode(200).withBody("{\"code\":0,\"message\":\"ok\",\"data\":null}"));
	
		client.movePad(padID, movePadId);
		String movePadText = (String) client.getText(movePadId).get("text");

		mockServer.when(HttpRequest.request().withMethod("POST").withPath("/api/1.2.13/setText")).respond(
			HttpResponse.response().withStatusCode(200).withBody("{\"code\":0,\"message\":\"ok\",\"data\":null}"));
	
		client.setText(movePadId, change);
		client.copyPad(movePadId, copyPadId, true);
	
		mockServer.clear(HttpRequest.request().withMethod("GET").withPath("/api/1.2.13/getText"));
	
		mockServer.when(HttpRequest.request().withMethod("GET").withPath("/api/1.2.13/getText"))
		.respond(HttpResponse.response().withStatusCode(200)
		.withBody("{\"code\":0,\"message\":\"ok\",\"data\":{\"text\":\"change\\n\"}}"));

		String copyPadTextForce = (String) client.getText(copyPadId).get("text");
		client.movePad(movePadId, copyPadId, true);
		String movePadTextForce = (String) client.getText(copyPadId).get("text");
   
		mockServer.when(HttpRequest.request().withMethod("POST").withPath("/api/1.2.13/deletePad")).respond(
			HttpResponse.response().withStatusCode(200).withBody("{\"code\":0,\"message\":\"ok\",\"data\":null}"));
	
		client.deletePad(copyPadId);
		client.deletePad(padID);
   
		assertEquals(keep + "\n", copyPadText);
		assertEquals(keep + "\n", movePadText);
   
		assertEquals(change + "\n", copyPadTextForce);
		assertEquals(change + "\n", movePadTextForce);

	}

   @Test
   public void create_pads_and_list_them() throws InterruptedException {

	   String pad1 = "integration-test-pad-1";
	   String pad2 = "integration-test-pad-2";

	   	mockServer.when(HttpRequest.request().withMethod("POST").withPath("/api/1.2.13/createPad")).respond(
			HttpResponse.response().withStatusCode(200).withBody("{\"code\":0,\"message\":\"ok\",\"data\":null}"));
	   
		client.createPad(pad1);
		client.createPad(pad2);

	   	Thread.sleep(100);
	   
	   	mockServer.when(HttpRequest.request().withMethod("GET").withPath("/api/1.2.13/listAllPads"))
			.respond(HttpResponse.response().withStatusCode(200).withBody(
			"{\"code\":0,\"message\":\"ok\",\"data\":{\"padIDs\":[\"integration-test-pad-1\",\"integration-test-pad-2\"]}}"));
	   
		List padIDs = (List) client.listAllPads().get("padIDs");

	   	mockServer.when(HttpRequest.request().withMethod("POST").withPath("/api/1.2.13/deletePad")).respond(
			HttpResponse.response().withStatusCode(200).withBody("{\"code\":0,\"message\":\"ok\",\"data\":null}"));
	   
		client.deletePad(pad1);
	   	client.deletePad(pad2);

	   	assertTrue(String.format("Size was %d", padIDs.size()), padIDs.size() >= 2);
	   	assertTrue(padIDs.contains(pad1));
	   	assertTrue(padIDs.contains(pad2));
   }

   @Test
	public void create_pad_and_chat_about_it() {

		String padID = "integration-test-pad-1";
		String user1 = "user1";
		String user2 = "user2";

		mockServer.when(HttpRequest.request().withMethod("POST").withPath("/api/1.2.13/createAuthorIfNotExistsFor"))
			.respond(HttpResponse.response().withStatusCode(200)
			.withBody("{\"code\":0,\"message\":\"ok\",\"data\":{\"authorID\":\"a.01\"}}"));

		Map response = client.createAuthorIfNotExistsFor(user1,"integration-author-1");		
		String author1Id = (String) response.get("authorID");

		mockServer.clear(HttpRequest.request().withMethod("POST").withPath("/api/1.2.13/createAuthorIfNotExistsFor"));

		mockServer.when(HttpRequest.request().withMethod("POST").withPath("/api/1.2.13/createAuthorIfNotExistsFor"))
			.respond(HttpResponse.response().withStatusCode(200)
			.withBody("{\"code\":0,\"message\":\"ok\",\"data\":{\"authorID\":\"a.02\"}}"));

		response = client.createAuthorIfNotExistsFor(user2, "integration-author-2");
		String author2Id = (String) response.get("authorID");

		mockServer.when(HttpRequest.request().withMethod("POST").withPath("/api/1.2.13/createPad")).respond(
			HttpResponse.response().withStatusCode(200).withBody("{\"code\":0,\"message\":\"ok\",\"data\":null}"));

		client.createPad(padID);

		try {

			mockServer.when(HttpRequest.request().withMethod("POST").withPath("/api/1.2.13/appendChatMessage"))
				.respond(HttpResponse.response().withStatusCode(200)
				.withBody("{\"code\":0,\"message\":\"ok\",\"data\":null}"));
	
			client.appendChatMessage(padID, "hi from user1", author1Id);
			client.appendChatMessage(padID, "hi from user2", author2Id,System.currentTimeMillis() / 1000L);
			client.appendChatMessage(padID, "bye from user1", author1Id,System.currentTimeMillis() / 1000L);

			mockServer.when(HttpRequest.request().withMethod("GET").withPath("/api/1.2.13/getChatHead"))
				.respond(HttpResponse.response().withStatusCode(200)
				.withBody("{\"code\":0,\"message\":\"ok\",\"data\":{\"chatHead\":2}}"));
	
			response = client.getChatHead(padID);
			long chatHead = (long) response.get("chatHead");
			assertEquals(2, chatHead);
   
			mockServer.when(HttpRequest.request().withMethod("GET").withPath("/api/1.2.13/getChatHistory"))
				.respond(HttpResponse.response().withStatusCode(200).withBody(
				"{\"code\":0,\"message\":\"ok\",\"data\":{\"messages\": [ \"hi from user1\",\"hi from user2\",\"bye from user1\"]}}"));
		
			response = client.getChatHistory(padID);
			List chatHistory = (List) response.get("messages");
			assertEquals(3, chatHistory.size());

			mockServer.clear(HttpRequest.request().withMethod("GET").withPath("/api/1.2.13/getChatHistory"));
	
			mockServer.when(HttpRequest.request().withMethod("GET").withPath("/api/1.2.13/getChatHistory"))
				.respond(HttpResponse.response().withStatusCode(200).withBody(
				"{\"code\":0,\"message\":\"ok\",\"data\":{\"messages\": [ {\"text\":\"hi from user1\"},{\"text\":\"hi from user2\"}]}}"));
	
			response = client.getChatHistory(padID, 0, 1);
			chatHistory = (List) response.get("messages");
			assertEquals(2, chatHistory.size());
			assertEquals("hi from user2", ((Map) chatHistory.get(1)).get("text"));
		} finally {
	
			mockServer.when(HttpRequest.request().withMethod("POST").withPath("/api/1.2.13/deletePad"))
				.respond(HttpResponse.response().withStatusCode(200)
				.withBody("{\"code\":0,\"message\":\"ok\",\"data\":null}"));
			
			client.deletePad(padID);
		}
   
	}
}