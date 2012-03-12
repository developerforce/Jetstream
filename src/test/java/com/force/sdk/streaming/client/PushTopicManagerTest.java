package com.force.sdk.streaming.client;

import com.force.sdk.streaming.model.PushTopic;
import com.google.inject.Inject;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;

/**
 * Tests the PushTopicManager data service to ensure data is modified correctly for
 * CRUD operations.
 * @author naamannewbold
 */
@Guice(modules = TestModule.class)
public class PushTopicManagerTest {

    @Inject
    PushTopicManager pushTopicManager;

    @Inject
    TestUtil testUtil;

    @BeforeClass
    public void clearPushTopics() {
        testUtil.clean(PushTopic.class, null);
    }

    /*
     * Ensures our jpa tests are independent of one another
     */
    @BeforeMethod
    public void clearEntityManager() {
        pushTopicManager.em.clear();
      if (pushTopicManager.em.getTransaction().isActive()) {
        pushTopicManager.em.getTransaction().rollback();
      }
    }

    /*
     * Ensure a topic can be retrieved by Id.
     */
    @Test(groups = "integration")
    public void testGettingTopicById() {
        PushTopic expectedPushTopic = pushTopicManager.createPushTopic(newPushTopic("testGettingTopicById"));
        PushTopic actualPushTopic = pushTopicManager.getTopicById(expectedPushTopic.getId());
        Assert.assertEquals(actualPushTopic, expectedPushTopic);
    }

    /*
     * Tests the creation of a new push topic.
     */
    @Test(groups = "integration")
    public void testCreatePushTopic() {
        PushTopic pushTopic = pushTopicManager.createPushTopic(newPushTopic("testCreatePushTopic"));

        Assert.assertTrue(pushTopic.getId().length() >= 15,
                "Expected a force.com Id of length 15 or more, but the id was " + pushTopic.getId());
        PushTopic expectedPushTopic = pushTopicManager.getTopicById(pushTopic.getId());
        Assert.assertEquals(expectedPushTopic.getName(), "testCreatePushTopic");
    }

    /*
     * Tests updating a push topic.
     */
    @Test(groups = "integration")
    public void testUpdatePushTopic() {
        PushTopic pushTopic = pushTopicManager.createPushTopic(newPushTopic("testUpdatePushTopic"));
        PushTopic updatedPushTopic = new PushTopic();
        updatedPushTopic.setId(pushTopic.getId());
        updatedPushTopic.setQuery("Select Id From Contact");
        pushTopicManager.updatePushTopic(updatedPushTopic.getId(), updatedPushTopic);

        PushTopic pushTopicResult = pushTopicManager.getTopicById(updatedPushTopic.getId());
        Assert.assertEquals(pushTopicResult.getName(), "testUpdatePushTopic");
        Assert.assertEquals(pushTopicResult.getQuery(), "Select Id From Contact");
    }

    /*
     * Tests deleting a push topic.
     */
    @Test(groups = "integration")
    public void testDeletePushTopic() {
        PushTopic pushTopic = pushTopicManager.createPushTopic(newPushTopic("testDeletePushTopic"));
        String testTopicId = pushTopic.getId();
        pushTopicManager.delete(testTopicId);

        Assert.assertEquals(pushTopicManager.getTopicById(testTopicId), null);
    }

    /*
     * Tests the values are copied correctly for an existing push topic entity.
     */
    @Test
    public void testStandardPushTopicValuesCopied() {
        PushTopic source = new PushTopic("source", StreamingApiVersion.V23.version, "Select Id From Source", "source");
        PushTopic destination = new PushTopic("dest", StreamingApiVersion.V24.version, "Select Id From Dest", "dest");
        pushTopicManager.copyStandardValues(source, destination);
        Assert.assertEquals(destination.getName(), "source");
        Assert.assertEquals(destination.getApiVersion(), StreamingApiVersion.V22.version);
        Assert.assertEquals(destination.getQuery(), "Select Id From Source");
        Assert.assertEquals(destination.getDescription(), "source");
    }

    /*
     * Test the values are not copied if they're null
     */
    @Test
    public void testStandardPushTopicValuesNotCopiedIfNull() {
        PushTopic source = new PushTopic(null, 0.0, null, null);
        PushTopic destination = new PushTopic("dest", StreamingApiVersion.V22.version, "Select Id From Dest", "destination");
        pushTopicManager.copyStandardValues(source, destination);
        Assert.assertEquals(destination.getName(), "dest");
        Assert.assertEquals(destination.getApiVersion(), StreamingApiVersion.V22.version);
        Assert.assertEquals(destination.getQuery(), "Select Id From Dest");
        Assert.assertEquals(destination.getDescription(), "destination");
    }

    private PushTopic newPushTopic(String topicName) {
        PushTopic pushTopic = new PushTopic();
        pushTopic.setApiVersion(StreamingApiVersion.LATEST.version);
        pushTopic.setName(topicName);
        pushTopic.setQuery("Select Id, Name From Account");
        return pushTopic;
    }
}
