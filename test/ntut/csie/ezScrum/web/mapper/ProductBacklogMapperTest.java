package ntut.csie.ezScrum.web.mapper;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;

import net.sf.antcontrib.logic.Throw;
import ntut.csie.ezScrum.issue.sql.service.core.Configuration;
import ntut.csie.ezScrum.issue.sql.service.core.IQueryValueSet;
import ntut.csie.ezScrum.issue.sql.service.internal.MySQLQuerySet;
import ntut.csie.ezScrum.issue.sql.service.tool.internal.MySQLControl;
import ntut.csie.ezScrum.refactoring.manager.ProjectManager;
import ntut.csie.ezScrum.test.CreateData.AddSprintToRelease;
import ntut.csie.ezScrum.test.CreateData.AddStoryToSprint;
import ntut.csie.ezScrum.test.CreateData.CreateProductBacklog;
import ntut.csie.ezScrum.test.CreateData.CreateProject;
import ntut.csie.ezScrum.test.CreateData.CreateRelease;
import ntut.csie.ezScrum.test.CreateData.CreateSprint;
import ntut.csie.ezScrum.test.CreateData.InitialSQL;
import ntut.csie.ezScrum.web.dataInfo.AttachFileInfo;
import ntut.csie.ezScrum.web.dataInfo.StoryInfo;
import ntut.csie.ezScrum.web.dataObject.AttachFileObject;
import ntut.csie.ezScrum.web.dataObject.StoryObject;
import ntut.csie.ezScrum.web.dataObject.TagObject;
import ntut.csie.ezScrum.web.dataObject.TaskObject;
import ntut.csie.ezScrum.web.databasEnum.IssueTypeEnum;
import ntut.csie.ezScrum.web.databasEnum.StoryEnum;
import ntut.csie.ezScrum.web.databasEnum.StoryTagRelationEnum;
import ntut.csie.ezScrum.web.databasEnum.TagEnum;
import ntut.csie.ezScrum.web.databasEnum.TaskEnum;
import ntut.csie.jcis.resource.core.IProject;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class ProductBacklogMapperTest {
	private CreateProject mCP;
	private CreateProductBacklog mCPB;
	private CreateRelease mCR;
	private AddSprintToRelease mASTR;
	private AddStoryToSprint mASTS;
	private int mProjectCount = 1;
	private int mStoryCount = 2;
	private ProductBacklogMapper mProductBacklogMapper = null;
	private Configuration mConfig = null;
	private MySQLControl mControl = null;
	
	private final String mFILE_NAME = "Initial.sql";
	private final String mFILE_TYPE = "sql/plain";
	
	@Before
	public void setUp() throws Exception {
		mConfig = new Configuration();
		mConfig.setTestMode(true);
		mConfig.save();
		
		mControl = new MySQLControl(mConfig);
		mControl.connect();
		
		// 初始化 SQL
		InitialSQL ini = new InitialSQL(mConfig);
		ini.exe();
		
		// 新增 Project
		mCP = new CreateProject(mProjectCount);
		mCP.exeCreate();
		
		// 新增 Story
		mCPB = new CreateProductBacklog(mStoryCount, mCP);
		mCPB.exe();
		
		mCR = new CreateRelease(1, mCP);
		mCR.exe();
		
		mASTR = new AddSprintToRelease(2, mCR, mCP);
		mASTR.exe();
		
		// 建立 productbacklog 物件
		IProject project = mCP.getProjectList().get(0);
		mProductBacklogMapper = new ProductBacklogMapper(project, mConfig.getUserSession());
		
		// ============= release ==============
		ini = null;
		project = null;
	}
	
	@After
	public void tearDown() throws Exception {
		// 初始化 SQL
		InitialSQL ini = new InitialSQL(mConfig);
		ini.exe();
		
		// 刪除外部檔案
		ProjectManager projectManager = new ProjectManager();
		projectManager.deleteAllProject();
		
		// 讓 config 回到  Production 模式
		mConfig.setTestMode(false);
		mConfig.save();
    	
    	// ============= release ==============
    	ini = null;
    	mCP = null;
    	mCPB = null;
    	mProductBacklogMapper = null;
    	projectManager = null;
    	mConfig = null;
    	mControl = null;
	}
	
	@Test
	public void testGetUnclosedStories() {
		long projectId = mCP.getAllProjects().get(0).getId();
		
		// add 5 unclosed stories,
		for (int i = 0; i< 5; i++) {
			StoryObject story = new StoryObject(projectId);
			story.setName("Story_" + i);
			story.setStatus(StoryObject.STATUS_UNCHECK);
			story.save();
		}
		
		// add 3 closed stories,
		for (int i = 0; i< 3; i++) {
			StoryObject story = new StoryObject(projectId);
			story.setName("Story_" + i);
			story.setStatus(StoryObject.STATUS_DONE);
			story.save();
		}
		
		ArrayList<StoryObject> closedStories = mProductBacklogMapper.getUnclosedStories();
		
		assertEquals(3, closedStories.size());
	}
	
	@Test
	public void testUpdateStoryRelation() {
		StoryObject story = mCPB.getStories().get(0);
		
		assertEquals("TEST_STORY_1", story.getName());
		assertEquals(StoryObject.DEFAULT_VALUE, story.getSprintId());
		assertEquals(100, story.getImportance());
		assertEquals(2, story.getEstimate());
		
		mProductBacklogMapper.updateStoryRelation(story.getId(), 1, 5, 10, new Date());
		
		story.reload();
		assertEquals("TEST_STORY_1", story.getName());
		assertEquals(1, story.getSprintId());
		assertEquals(10, story.getImportance());
		assertEquals(5, story.getEstimate());
	}
	
	@Test
	public void testGetStoriesByRelease() {
		
	}
	
	@Test
	public void testUpdateStory() {
		StoryObject story = mCPB.getStories().get(0);
		
		assertEquals("TEST_STORY_1", story.getName());
		assertEquals("TEST_STORY_NOTE_1", story.getNotes());
		assertEquals("TEST_STORY_DEMO_1", story.getHowToDemo());
		assertEquals(StoryObject.DEFAULT_VALUE, story.getSprintId());
		assertEquals(100, story.getImportance());
		assertEquals(2, story.getEstimate());
		assertEquals(50, story.getValue());
		
		StoryInfo storyInfo = new StoryInfo();
		storyInfo.name = "NEW_STORY_1";
		storyInfo.notes = "NEW_NOTE_1";
		storyInfo.howToDemo = "NEW_DEMO_1";
		storyInfo.sprintId = 1;
		storyInfo.estimate = 13;
		storyInfo.importance = 0;
		storyInfo.value = 100;
		
		mProductBacklogMapper.updateStory(storyInfo);
		
		story.reload();
		assertEquals("NEW_STORY_1", story.getName());
		assertEquals("NEW_NOTE_1", story.getNotes());
		assertEquals("NEW_DEMO_1", story.getHowToDemo());
		assertEquals(1, story.getSprintId());
		assertEquals(0, story.getImportance());
		assertEquals(13, story.getEstimate());
		assertEquals(100, story.getValue());
	}
	
	@Test
	public void testAddStory() throws SQLException {
		StoryInfo storyInfo = new StoryInfo();
		storyInfo.name = "TEST_NAME";
		storyInfo.howToDemo = "TEST_HOW_TO_DEMO";
		storyInfo.notes = "TEST_NOTES";
		storyInfo.estimate = 1;
		storyInfo.value = 2;
		storyInfo.importance = 3;
		
		StoryObject story = mProductBacklogMapper.addStory(storyInfo);
		story = StoryObject.get(story.getId());
		
		assertEquals(storyInfo.name, story.getName());
		assertEquals(storyInfo.notes, story.getNotes());
		assertEquals(storyInfo.howToDemo, story.getHowToDemo());
		assertEquals(storyInfo.estimate, story.getEstimate());
		assertEquals(storyInfo.importance, story.getImportance());
		assertEquals(storyInfo.value, story.getValue());
		assertEquals(StoryObject.STATUS_UNCHECK, story.getStatus());
		assertEquals(3, story.getSerialId());
		assertEquals(StoryObject.DEFAULT_VALUE, story.getSprintId());
		assertEquals(1, story.getHistories().size());
	}
	
	@Test // 測試上傳檔案到一筆 Story 是否成功
	public void testAddAttachFile_Story() {
		StoryObject story = mCPB.getStories().get(0);
		
		addAttachFile(mProductBacklogMapper, story.getId(), IssueTypeEnum.TYPE_STORY);
		
		story.reload();
		AttachFileObject ActualFile = story.getAttachFiles().get(0);
		
		assertEquals(1, story.getAttachFiles().size());
		assertEquals(mFILE_NAME, ActualFile.getName());
		assertEquals(mFILE_TYPE, ActualFile.getContentType());
		assertEquals(story.getId(), ActualFile.getId());
		
		// ============= release ==============
		story = null;
		ActualFile = null;
	}
	
	@Test // 測試刪除一筆 Story 的檔案
	public void testDeleteAttachFile_Story() {
		StoryObject story = mCPB.getStories().get(0);		
		
		addAttachFile(mProductBacklogMapper, story.getId(), IssueTypeEnum.TYPE_STORY);
		
		story.reload();
		AttachFileObject ActualFile = story.getAttachFiles().get(0);
		
		assertEquals(1, story.getAttachFiles().size());
		assertEquals(mFILE_NAME, ActualFile.getName());
		assertEquals(mFILE_TYPE, ActualFile.getContentType());
		assertEquals(story.getId(), ActualFile.getId());
		
		// 刪除此 issue 的檔案
		mProductBacklogMapper.deleteAttachFile(ActualFile.getId());
		story.reload();
		assertEquals(0, story.getAttachFiles().size());
		
		// ============= release ==============
		story = null;
		ActualFile = null;		
	}
	
	@Test
	public void testGetAttachfile_Story() {
		StoryObject story = mCPB.getStories().get(0);
		
		addAttachFile(mProductBacklogMapper, story.getId(), IssueTypeEnum.TYPE_STORY);
		
		story.reload();
		AttachFileObject IssueFile = story.getAttachFiles().get(0);
		
		assertEquals(1, story.getAttachFiles().size());
		assertEquals(mFILE_NAME, IssueFile.getName());
		assertEquals(mFILE_TYPE, IssueFile.getContentType());
		assertEquals(story.getId(), IssueFile.getId());

		// ============= release ==============
		story = null;
	}
	
	@Test
	public void testModifyStoryName_Existing() {
		StoryObject story = mCPB.getStories().get(0);
		mProductBacklogMapper.modifyStoryName(story.getId(), "NEW_NAME", new Date());
		story.reload();
		assertEquals("NEW_NAME", story.getName());
	}
	
	/*
	 * Modify non-existing story's name should pass and no error
	 */
	@Test
	public void testModifyStoryName_No_Existing() {
		mProductBacklogMapper.modifyStoryName(100, "NEW_NAME", new Date());
	}
	
	@Test
	public void testDeleteStory_Existing() throws SQLException {
		// get story
		StoryObject story = mCPB.getStories().get(0);
		
		// delete story
		mProductBacklogMapper.deleteStory(story.getId());
		// get data from database
		IQueryValueSet valueSet = new MySQLQuerySet();
		valueSet.addTableName(StoryEnum.TABLE_NAME);
		valueSet.addEqualCondition(StoryEnum.ID, story.getId());
		String query = valueSet.getSelectQuery();
		ResultSet result = mControl.executeQuery(query);
		assertFalse(result.next());
	}
	
	@Test
	public void testDeleteStory_No_Existing() {
		// test data
		long nonExistStoryId = 5;
		
		// delete story
		try {
			mProductBacklogMapper.deleteStory(nonExistStoryId);
		} catch (Exception e) {
			Assert.fail();
		}
		assertTrue(true);
	}
	
	@Test
	public void testRemoveTask() throws SQLException {
		// get project
		IProject iProject = mCP.getProjectList().get(0);
		ProjectObject project = ProjectObject.get(iProject.getName());
		// test data
		String taskName = "TEST_TASK_1";
		int taskEstimate = 13;
		
		// get story
		StoryObject story = mCPB.getStories().get(0);
		TaskObject task = new TaskObject(project.getId());
		task.setName(taskName)
		    .setEstimate(taskEstimate)
		    .setStoryId(story.getId())
		    .save();
		assertEquals(story.getId(), task.getStoryId());
		
		// removeTask
		mProductBacklogMapper.removeTask(task.getId(), story.getId());
		// get data from database
		IQueryValueSet valueSet = new MySQLQuerySet();
		valueSet.addTableName(TaskEnum.TABLE_NAME);
		valueSet.addEqualCondition(TaskEnum.ID, task.getId());
		String query = valueSet.getSelectQuery();

		ResultSet result = mControl.executeQuery(query);

		TaskObject taskDB = null;
		
		if (result.next()) {
			taskDB = new TaskObject(result.getLong(TaskEnum.ID),
			        result.getLong(TaskEnum.SERIAL_ID),
			        result.getLong(TaskEnum.PROJECT_ID));
			taskDB.setName(result.getString(TaskEnum.NAME))
			      .setHandlerId(result.getLong(TaskEnum.HANDLER_ID))
			      .setEstimate(result.getInt(TaskEnum.ESTIMATE))
			      .setRemains(result.getInt(TaskEnum.REMAIN))
			      .setActual(result.getInt(TaskEnum.ACTUAL))
			      .setStatus(result.getInt(TaskEnum.STATUS))
			      .setNotes(result.getString(TaskEnum.NOTES))
			      .setStoryId(result.getLong(TaskEnum.STORY_ID))
			      .setCreateTime(result.getLong(TaskEnum.CREATE_TIME))
			      .setUpdateTime(result.getLong(TaskEnum.UPDATE_TIME));
		}
		// assert
		assertEquals(-1, taskDB.getStoryId());
	}
	
	@Test
	public void testAddNewTag() throws SQLException {
		// get project
		IProject iProject = mCP.getProjectList().get(0);
		ProjectObject project = ProjectObject.get(iProject.getName());
		// test data
		String tagName = "TEST_TAG_NAME_1";
		
		// addNewTag
		long tagId = mProductBacklogMapper.addNewTag(tagName);
		assertNotSame(-1, tagId);
		
		// 從資料庫撈出資料
		IQueryValueSet valueSet = new MySQLQuerySet();
		valueSet.addTableName(TagEnum.TABLE_NAME);
		valueSet.addEqualCondition(TagEnum.ID, tagId);
		String query = valueSet.getSelectQuery();
		ResultSet result = mControl.executeQuery(query);

		TagObject tagObject = null;

		if (result.next()) {
			long id = result.getLong(TagEnum.ID);
			String name = result.getString(TagEnum.NAME);
			long projectId = result.getLong(TagEnum.PROJECT_ID);
			long createTime = result.getLong(TagEnum.CREATE_TIME);
			long updateTime = result.getLong(TagEnum.UPDATE_TIME);
			tagObject = new TagObject(id, name, projectId);
			tagObject.setCreateTime(createTime).setUpdateTime(updateTime);
		}
		
		// assert
		assertEquals(tagName, tagObject.getName());
		assertEquals(project.getId(), tagObject.getProjectId());
	}
	
	@Test
	public void testDeleteTag() throws SQLException {
		// get project
		IProject iProject = mCP.getProjectList().get(0);
		ProjectObject project = ProjectObject.get(iProject.getName());
		// test data
		String tagName = "TEST_TAG_NAME_1";
		// create tag
		TagObject tag = new TagObject(tagName, project.getId());
		tag.save();

		// get story
		StoryObject story = mCPB.getStories().get(0);
		// add tag
		story.addTag(tag.getId());
		// 從資料庫撈出資料
		IQueryValueSet valueSet = new MySQLQuerySet();
		valueSet.addTableName(TagEnum.TABLE_NAME);
		valueSet.addEqualCondition(TagEnum.ID, tag.getId());
		String query = valueSet.getSelectQuery();
		ResultSet result = mControl.executeQuery(query);
		assertTrue(result.next());
		
		// deleteTag
		mProductBacklogMapper.deleteTag(tag.getId());
		// assert
		valueSet = new MySQLQuerySet();
		valueSet.addTableName(TagEnum.TABLE_NAME);
		valueSet.addEqualCondition(TagEnum.ID, tag.getId());
		query = valueSet.getSelectQuery();
		result = mControl.executeQuery(query);
		assertFalse(result.next());
	}
	
	@Test
	public void testGetTags() {
		// get project
		IProject iProject = mCP.getProjectList().get(0);
		ProjectObject project = ProjectObject.get(iProject.getName());
		// test data
		String tagName = "TEST_TAG_NAME_";
		// create 3 tag
		for (int i = 0; i < 3; i++) {
			TagObject tag = new TagObject(tagName + (i + 1), project.getId());
			tag.save();
		}
		
		// getTags
		ArrayList<TagObject> tags = mProductBacklogMapper.getTags();
		// assert
		assertEquals(3, tags.size());
		
		for (int i = 0; i < 3; i++) {
			assertEquals(tagName + (i + 1), tags.get(i).getName());
		}
	}
	
	@Test
	public void testAddStoryTag() throws SQLException {
		// get project
		IProject iProject = mCP.getProjectList().get(0);
		ProjectObject project = ProjectObject.get(iProject.getName());
		// test data
		String tagName = "TEST_TAG_NAME_1";
		// create tag
		TagObject tag = new TagObject(tagName, project.getId());
		tag.save();

		// get story
		StoryObject story = mCPB.getStories().get(0);
		// addStoryTag
		mProductBacklogMapper.addStoryTag(story.getId(), tag.getId());
		// 從資料庫撈出資料
		IQueryValueSet valueSet = new MySQLQuerySet();
		valueSet.addTableName(StoryTagRelationEnum.TABLE_NAME);
		valueSet.addEqualCondition(TagEnum.ID, tag.getId());
		valueSet.addEqualCondition(StoryEnum.ID, story.getId());
		String query = valueSet.getSelectQuery();
		ResultSet result = mControl.executeQuery(query);
		assertTrue(result.next());
	}
	
	@Test
	public void testRemoveStoryTag() throws SQLException {
		// get project
		IProject iProject = mCP.getProjectList().get(0);
		ProjectObject project = ProjectObject.get(iProject.getName());
		// test data
		String tagName = "TEST_TAG_NAME_1";
		// create tag
		TagObject tag = new TagObject(tagName, project.getId());
		tag.save();
		
		// get story
		StoryObject story = mCPB.getStories().get(0);
		// add tag
		story.addTag(tag.getId());
		// 從資料庫撈出資料
		IQueryValueSet valueSet = new MySQLQuerySet();
		valueSet.addTableName(StoryTagRelationEnum.TABLE_NAME);
		valueSet.addEqualCondition(TagEnum.ID, tag.getId());
		valueSet.addEqualCondition(StoryEnum.ID, story.getId());
		String query = valueSet.getSelectQuery();
		ResultSet result = mControl.executeQuery(query);
		assertTrue(result.next());
		
		// remove story tag
		mProductBacklogMapper.removeStoryTag(story.getId(), tag.getId());
		
		// 從資料庫撈出資料
		valueSet = new MySQLQuerySet();
		valueSet.addTableName(StoryTagRelationEnum.TABLE_NAME);
		valueSet.addEqualCondition(TagEnum.ID, tag.getId());
		valueSet.addEqualCondition(StoryEnum.ID, story.getId());
		query = valueSet.getSelectQuery();
		result = mControl.executeQuery(query);
		assertFalse(result.next());
	}
	
	@Test
	public void testUpdateTag() throws SQLException {
		// get project
		IProject iProject = mCP.getProjectList().get(0);
		ProjectObject project = ProjectObject.get(iProject.getName());
		// test data
		String tagName = "TEST_TAG_NAME_1";
		String newTagName = "TEST_TAG_NAME_NEW";
		// create tag
		TagObject tag = new TagObject(tagName, project.getId());
		tag.save();
		
		// test Update Tag
		mProductBacklogMapper.updateTag(tag.getId(), newTagName);
		
		// 從資料庫撈出資料
		IQueryValueSet valueSet = new MySQLQuerySet();
		valueSet.addTableName(TagEnum.TABLE_NAME);
		valueSet.addEqualCondition(TagEnum.ID, tag.getId());
		String query = valueSet.getSelectQuery();
		ResultSet result = mControl.executeQuery(query);
		TagObject tagObject = null;
		
		if (result.next()) {
			long id = result.getLong(TagEnum.ID);
			String name = result.getString(TagEnum.NAME);
			long projectId = result.getLong(TagEnum.PROJECT_ID);
			long createTime = result.getLong(TagEnum.CREATE_TIME);
			long updateTime = result.getLong(TagEnum.UPDATE_TIME);
			tagObject = new TagObject(id, name, projectId);
			tagObject.setCreateTime(createTime).setUpdateTime(updateTime);
		}
		
		// assert
		assertNotNull(tagObject);
		assertEquals(newTagName, tagObject.getName());
	}
	
	@Test
	public void testIsTagExist() {
		// get project
		IProject iProject = mCP.getProjectList().get(0);
		ProjectObject project = ProjectObject.get(iProject.getName());
		// test data
		String tagName = "TEST_TAG_NAME_1";
		// create tag
		TagObject tag = new TagObject(tagName, project.getId());
		tag.save();
		
		// test isTagExist
		boolean isTagExist = mProductBacklogMapper.isTagExist(tagName);
		assertTrue(isTagExist);
		
		// test wrong tag name
		isTagExist = mProductBacklogMapper.isTagExist(tagName + "1");
		assertFalse(isTagExist);
	}
	
	@Test
	public void testGetTagByName() {
		// get project
		IProject iProject = mCP.getProjectList().get(0);
		ProjectObject project = ProjectObject.get(iProject.getName());
		// test data
		String tagName = "TEST_TAG_NAME_1";
		// create tag
		TagObject tag = new TagObject(tagName, project.getId());
		tag.save();
		
		// test get tag by name
		TagObject targetTag = mProductBacklogMapper.getTagByName(tagName);
		// assert
		assertNotNull(targetTag);
		assertEquals(tagName, targetTag.getName());
		
		// wrong name test
		targetTag = mProductBacklogMapper.getTagByName(tagName + "1");
		// assert
		assertNull(targetTag);
	}
	
	private void addAttachFile(ProductBacklogMapper mapper, long issueId, int issutType) {
		AttachFileInfo attachFileInfo = new AttachFileInfo();
        attachFileInfo.issueId = issueId;
        attachFileInfo.issueType = issutType;
        attachFileInfo.name = mFILE_NAME;
        attachFileInfo.contentType = mFILE_TYPE;
        attachFileInfo.projectName = mCP.getProjectList().get(0).getName();
        mapper.addAttachFile(attachFileInfo);
	}
}
