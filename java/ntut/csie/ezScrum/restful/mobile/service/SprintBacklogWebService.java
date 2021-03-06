package ntut.csie.ezScrum.restful.mobile.service;

import java.util.ArrayList;

import ntut.csie.ezScrum.restful.mobile.support.ConvertSprintBacklog;
import ntut.csie.ezScrum.web.dataObject.SprintObject;
import ntut.csie.ezScrum.web.dataObject.StoryObject;
import ntut.csie.ezScrum.web.dataObject.TaskObject;
import ntut.csie.ezScrum.web.logic.SprintBacklogLogic;
import ntut.csie.ezScrum.web.mapper.SprintBacklogMapper;
import ntut.csie.jcis.account.core.LogonException;

import org.codehaus.jettison.json.JSONException;

public class SprintBacklogWebService extends ProjectWebService {
	SprintBacklogMapper mSprintBacklogMapper;
	SprintBacklogLogic mSprintBacklogLogic;

	public SprintBacklogWebService(String username, String userpwd, String projectName, long sprintId) throws LogonException {
		super(username, userpwd, projectName);
		mSprintBacklogLogic = new SprintBacklogLogic(getAllProjects().get(0), sprintId);
		mSprintBacklogMapper = mSprintBacklogLogic.getSprintBacklogMapper();
	}

	public SprintBacklogWebService(String username, String userpwd, String projectName) throws LogonException {
		super(username, userpwd, projectName);
		mSprintBacklogLogic = new SprintBacklogLogic(getAllProjects().get(0), -1);
		mSprintBacklogMapper = mSprintBacklogLogic.getSprintBacklogMapper();
	}

	public String getStoriesIdJsonStringInSprint() throws JSONException {
		return ConvertSprintBacklog.getStoriesIdJsonStringInSprint(mSprintBacklogLogic.getStoriesSortedByIdInSprint());
	}

	public String getTasksIdJsonStringInStory(long storyId) throws JSONException {
		SprintObject sprint = mSprintBacklogMapper.getSprint();
		StoryObject story = mSprintBacklogMapper.getStory(storyId);
		if (sprint.containsStory(story)) {
			ArrayList<TaskObject> tasks = story.getTasks();
			return ConvertSprintBacklog.getTasksIdJsonStringInStory(storyId, tasks);
		}
		return "";
	}

	/**
	 * 取得task history list
	 * 
	 * @param taskId
	 * @return
	 * @throws JSONException
	 */
	public String getTaskHsitoryJsonString(long taskId) throws JSONException {
		TaskObject task = TaskObject.get(taskId);
		SprintObject sprint = mSprintBacklogMapper.getSprint();
		if (sprint.containsTask(task)) {
			return ConvertSprintBacklog.getTaskHistoriesJsonString(task.getHistories());
		}
		return "";
	}

	/**
	 * 取得task information
	 * 
	 * @param taskId
	 * @return
	 * @throws JSONException
	 */
	public String getTaskJsonString(long taskId) throws JSONException {
		TaskObject task = TaskObject.get(taskId);
		SprintObject sprint = mSprintBacklogMapper.getSprint();
		if (sprint.containsTask(task)) {
			return ConvertSprintBacklog.getTaskJsonString(task);
		}
		return "";
	}

	/**
	 * 取得 Sprint 的 Sprint Backlog(Sprint 底下的 Story 及 Task)
	 * 
	 * @return
	 * @throws JSONException
	 */
	public String getSprintBacklogJsonString() throws JSONException {
		SprintObject sprint = mSprintBacklogMapper.getSprint();
		return ConvertSprintBacklog.getSprintBacklogJsonString(sprint);
	}
	
	/**
	 * get Sprint and sprint 底下的 Story 及 Task JSON string
	 * @return
	 */
	public String getTaskboardJsonString() {
		SprintObject sprint = mSprintBacklogMapper.getSprint();
		return new ConvertSprintBacklog().getTaskboardJsonString(sprint);
	}
}
