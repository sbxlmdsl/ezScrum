package ntut.csie.ezScrum.web.dataObject;

import java.util.Date;

import ntut.csie.ezScrum.dao.ReleaseDAO;
import ntut.csie.jcis.core.util.DateUtil;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

public class ReleaseObject implements IBaseObject {
	private final static int DEFAULT_VALUE = -1;

	private long mId = DEFAULT_VALUE;
	private long mSerialId = DEFAULT_VALUE;
	private long mProjectId = DEFAULT_VALUE;

	private String mName = "";
	private String mDescription = "";
	private Date mStartDate = new Date();
	private Date mDueDate = new Date();

	private long mCreateTime = 0;
	private long mUpdateTime = 0;
	
	public ReleaseObject(long projectId) {
		mProjectId = projectId;
	}
	
	public ReleaseObject(long id, Long serialId, long projectId) {
		mId = id;
		mSerialId = serialId;
		mProjectId = projectId;
	}
	
	public ReleaseObject setName(String name) {
		mName = name;
		return this;
	}
	
	public ReleaseObject setDescription(String description) {
		mDescription = description;
		return this;
	}
	
	public ReleaseObject setStartDate(String startDate) {
		mStartDate = DateUtil.dayFilter(startDate);
		return this;
	}

	public ReleaseObject setDueDate(String dueDate) {
		mDueDate = DateUtil.dayFilter(dueDate);
		return this;
	}
	
	public ReleaseObject setCreateTime(long createTime) {
		mCreateTime = createTime;
		return this;
	}

	public ReleaseObject setUpdateTime(long updateTime) {
		mUpdateTime = updateTime;
		return this;
	}
	
	public long getId() {
		return mId;
	}
	
	public long getSerialId() {
		return mSerialId;
	}

	public long getProjectId() {
		return mProjectId;
	}
	
	public String getName() {
		return mName;
	}
	
	public String getDescription() {
		return mDescription;
	}
	
	public String getStartDateString() {
		return DateUtil.formatBySlashForm(mStartDate);
	}

	public String getDueDateString() {
		return DateUtil.formatBySlashForm(mDueDate);
	}
	
	public long getCreateTime() {
		return mCreateTime;
	}

	public long getUpdateTime() {
		return mUpdateTime;
	}

	@Override
	public void save() {
		if (exists()) {
			mUpdateTime = System.currentTimeMillis();
			doUpdate();
		} else {
			doCreate();
		}
	}

	public void save(long specificTime) {
		if (exists()) {
			mUpdateTime = specificTime;
			doUpdate(specificTime);
		} else {
			doCreate();
		}
	}

	@Override
	public void reload() {
		if (exists()) {
			ReleaseObject sprint = ReleaseDAO.getInstance().get(mId);
			resetData(sprint);
		}
	}

	@Override
	public boolean delete() {
		boolean success = ReleaseDAO.getInstance().delete(mId);
		if (success) {
			mId = DEFAULT_VALUE;
			mSerialId = DEFAULT_VALUE;
			mProjectId = DEFAULT_VALUE;
		}
		return success;
	}
	
	private void doCreate() {
		mId = ReleaseDAO.getInstance().create(this);
		reload();
	}

	private void doUpdate() {
		ReleaseDAO.getInstance().update(this);
	}

	private void doUpdate(long specificTime) {
		mUpdateTime = specificTime;
		ReleaseDAO.getInstance().update(this);
	}
	
	private void resetData(ReleaseObject release) {
		mId = release.getId();
		mProjectId = release.getProjectId();
		mSerialId = release.getSerialId();

		setName(release.getName());
		setDescription(release.getDescription());
		setStartDate(release.getStartDateString());
		setDueDate(release.getDueDateString());
		setCreateTime(release.getCreateTime());
		setUpdateTime(release.getUpdateTime());
	}
	
	private boolean exists() {
		ReleaseObject release = ReleaseDAO.getInstance().get(mId);
		return release != null;
	}

	@Override
    public JSONObject toJSON() throws JSONException {
		JSONObject release = new JSONObject();
	    return null;
    }
	
}
