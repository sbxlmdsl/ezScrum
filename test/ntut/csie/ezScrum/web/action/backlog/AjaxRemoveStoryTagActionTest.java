package ntut.csie.ezScrum.web.action.backlog;

import java.io.File;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import ntut.csie.ezScrum.dao.TagDAO;
import ntut.csie.ezScrum.issue.sql.service.core.Configuration;
import ntut.csie.ezScrum.issue.sql.service.core.InitialSQL;
import ntut.csie.ezScrum.test.CreateData.CreateProductBacklog;
import ntut.csie.ezScrum.test.CreateData.CreateProject;
import ntut.csie.ezScrum.web.dataObject.ProjectObject;
import ntut.csie.ezScrum.web.dataObject.StoryObject;
import ntut.csie.ezScrum.web.dataObject.TagObject;
import servletunit.struts.MockStrutsTestCase;

public class AjaxRemoveStoryTagActionTest extends MockStrutsTestCase {
	private CreateProject mCP;
	private CreateProductBacklog mCPB;
	private Configuration mConfig;
	private ProjectObject mProject;
	private int mStoriesCount = 3;

	public AjaxRemoveStoryTagActionTest(String testMethod) {
		super(testMethod);
	}

	@Before
	public void setUp() throws Exception {
		mConfig = new Configuration();
		mConfig.setTestMode(true);
		mConfig.save();

		// 初始化 SQL
		InitialSQL ini = new InitialSQL(mConfig);
		ini.exe();

		// create project
		mCP = new CreateProject(1);
		mCP.exeCreateForDb();

		// create story
		mCPB = new CreateProductBacklog(mStoriesCount, mCP);
		mCPB.exe();

		// get project
		mProject = mCP.getAllProjects().get(0);

		super.setUp();

		// 設定讀取的 struts-config 檔案路徑
		setContextDirectory(new File(mConfig.getBaseDirPath() + "/WebContent"));
		setServletConfigFile("/WEB-INF/struts-config.xml");
		setRequestPathInfo("/AjaxRemoveStoryTag");
	}

	@After
	public void tearDown() throws Exception {
		// 初始化 SQL
		InitialSQL ini = new InitialSQL(mConfig);
		ini.exe();

		mConfig.setTestMode(false);
		mConfig.save();

		// ============= release ==============
		ini = null;
		mCP = null;
		mCPB = null;
		mConfig = null;
		super.tearDown();
	}
	
	@Test
	public void testRemoveStoryTag() {
		// Story
		StoryObject story = mCPB.getStories().get(0);
		// create tag
		TagObject tag = new TagObject("TEST_STORY_TAG", mProject.getId());
		tag.save();
		
		// add tag to story
		TagDAO.getInstance().addTagToStory(story.getId(), tag.getId());
		// assert tag exist
		assertEquals(1, story.getTags().size());

		// ================== set parameter info ====================
		addRequestParameter("storyId", String.valueOf(story.getId()));
		addRequestParameter("tagId", String.valueOf(tag.getId()));

		// ================ set session info ========================
		request.getSession().setAttribute("UserSession", mConfig.getUserSession());
		request.getSession().setAttribute("Project", mProject);

		// ================ set session info ========================
		// SessionManager 會對URL的參數作分析 ,未帶入此參數無法存入 session
		request.setHeader("Referer", "?PID=" + mProject.getName());

		// 執行 action
		actionPerform();
		// 驗證回傳
		verifyNoActionErrors();
		
		// reload story
		story.reload();
		
		// assert
		assertEquals(0, story.getTags().size());
	}
}
