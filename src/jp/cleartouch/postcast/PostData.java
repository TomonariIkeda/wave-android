package jp.cleartouch.postcast;

public class PostData {

	private int y; 
	private int postedAt; // posted time in sec
	private String text;
	private String userName;
	private String createdDate;
	private String createdTime;
	private int thumbResId;

	public PostData(int y, int posted_at, String text, int thumb_res_id, String user_name, String created_date, String created_time){
		this.setY(y);
		this.setPostedAt(posted_at);
		this.text = text;
		this.thumbResId = thumb_res_id;
		this.userName = user_name;
		this.createdDate = created_date;
		this.createdTime = created_time;
	}
	
	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getCreatedDate() {
		return createdDate;
	}

	public void setCreatedDate(String createdDate) {
		this.createdDate = createdDate;
	}

	public String getCreatedTime() {
		return createdTime;
	}

	public void setCreatedTime(String createdTime) {
		this.createdTime = createdTime;
	}

	public int getThumbResId() {
		return thumbResId;
	}

	public void setThumbResId(int thumbResId) {
		this.thumbResId = thumbResId;
	}

	public int getY() {
		return y;
	}

	public void setY(int y) {
		this.y = y;
	}

	public int getPostedAt() {
		return postedAt;
	}

	public void setPostedAt(int postedAt) {
		this.postedAt = postedAt;
	}
	
}
