package com.zazarie.domain.reddit;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Date;

public class Data  implements Serializable  {
	String modhash;
	String domain;
	String banned_by;
	String name;
	Media_Embed media_embed;	
	Children children[];
	String subreddit;
	String selftext_html;
	String selftext;
	int	likes;
	String link_flair_text;
	String id;
	boolean clicked;
	String title;
	int score;
	String approved_by;
	boolean over_18;
	boolean hidden;
	String thumbnail;
	String subreddit_id;
	boolean edited;
	String link_flair_css_class;
	String author_flair_css_class;
	String author;
	String author_flair_text;
	String url;
	String created_utc;
	String permalink;
	boolean is_self;
	String created;
	String after;
	Media media;
	String before;
	String description;
	String header_img;
	String header_title;
	String description_html;
	String public_descrition;
	String accounts_active;
	int header_size [];
	int subscribers;
	String kind;
	String display_name;
	
	
	
	
	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getHeader_img() {
		return header_img;
	}

	public void setHeader_img(String header_img) {
		this.header_img = header_img;
	}

	public String getHeader_title() {
		return header_title;
	}

	public void setHeader_title(String header_title) {
		this.header_title = header_title;
	}

	public String getDescription_html() {
		return description_html;
	}

	public void setDescription_html(String description_html) {
		this.description_html = description_html;
	}

	public String getPublic_descrition() {
		return public_descrition;
	}

	public void setPublic_descrition(String public_descrition) {
		this.public_descrition = public_descrition;
	}

	public String getAccounts_active() {
		return accounts_active;
	}

	public void setAccounts_active(String accounts_active) {
		this.accounts_active = accounts_active;
	}

	public int[] getHeader_size() {
		return header_size;
	}

	public void setHeader_size(int[] header_size) {
		this.header_size = header_size;
	}

	public int getSubscribers() {
		return subscribers;
	}

	public void setSubscribers(int subscribers) {
		this.subscribers = subscribers;
	}

	public String getKind() {
		return kind;
	}

	public void setKind(String kind) {
		this.kind = kind;
	}

	public String getDisplay_name() {
		return display_name;
	}

	public void setDisplay_name(String display_name) {
		this.display_name = display_name;
	}

	public String getAfter() {
		return after;
	}

	public void setAfter(String after) {
		this.after = after;
	}

	public String getBefore() {
		return before;
	}

	public void setBefore(String before) {
		this.before = before;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getPermalink() {
		return permalink;
	}

	public void setPermalink(String permalink) {
		this.permalink = permalink;
	}

	public boolean isIs_self() {
		return is_self;
	}

	public void setIs_self(boolean is_self) {
		this.is_self = is_self;
	}

	int ups;
	int num_comments;
	String num_reports;
	String distinguished;
	int downs;
	boolean saved;

	public Children[] getChildren() {
		return children;
	}

	public void setChildren(Children[] children) {
		this.children = children;
	}

	public String getDomain() {
		return domain;
	}

	public void setDomain(String domain) {
		this.domain = domain;
	}

	public String getBanned_by() {
		return banned_by;
	}

	public void setBanned_by(String banned_by) {
		this.banned_by = banned_by;
	}


	public Media_Embed getMedia_embed() {
		return media_embed;
	}

	public void setMedia_embed(Media_Embed media_embed) {
		this.media_embed = media_embed;
	}

	public String getModhash() {
		return modhash;
	}

	public void setModhash(String modhash) {
		this.modhash = modhash;
	}

	public String getSubreddit() {
		return subreddit;
	}

	public void setSubreddit(String subreddit) {
		this.subreddit = subreddit;
	}

	public String getSelftext_html() {
		return selftext_html;
	}

	public void setSelftext_html(String selftext_html) {
		this.selftext_html = selftext_html;
	}

	public String getSelftext() {
		return selftext;
	}

	public void setSelftext(String selftext) {
		this.selftext = selftext;
	}

	public int getLikes() {
		return likes;
	}

	public void setLikes(int likes) {
		this.likes = likes;
	}

	public String getLink_flair_text() {
		return link_flair_text;
	}

	public void setLink_flair_text(String link_flair_text) {
		this.link_flair_text = link_flair_text;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public boolean isClicked() {
		return clicked;
	}

	public void setClicked(boolean clicked) {
		this.clicked = clicked;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}


	public int getScore() {
		return score;
	}

	public void setScore(int score) {
		this.score = score;
	}

	public String getApproved_by() {
		return approved_by;
	}

	public void setApproved_by(String approved_by) {
		this.approved_by = approved_by;
	}

	public boolean isOver_18() {
		return over_18;
	}

	public void setOver_18(boolean over_18) {
		this.over_18 = over_18;
	}

	public boolean isHidden() {
		return hidden;
	}

	public void setHidden(boolean hidden) {
		this.hidden = hidden;
	}

	public String getThumbnail() {
		return thumbnail;
	}

	public void setThumbnail(String thumbnail) {
		this.thumbnail = thumbnail;
	}

	public String getSubreddit_id() {
		return subreddit_id;
	}

	public void setSubreddit_id(String subreddit_id) {
		this.subreddit_id = subreddit_id;
	}

	public boolean isEdited() {
		return edited;
	}

	public void setEdited(boolean edited) {
		this.edited = edited;
	}

	public String getLink_flair_css_class() {
		return link_flair_css_class;
	}

	public void setLink_flair_css_class(String link_flair_css_class) {
		this.link_flair_css_class = link_flair_css_class;
	}

	public String getAuthor_flair_css_class() {
		return author_flair_css_class;
	}

	public void setAuthor_flair_css_class(String author_flair_css_class) {
		this.author_flair_css_class = author_flair_css_class;
	}

	public String getAuthor() {
		return author;
	}

	public void setAuthor(String author) {
		this.author = author;
	}

	public String getAuthor_flair_text() {
		return author_flair_text;
	}

	public void setAuthor_flair_text(String author_flair_text) {
		this.author_flair_text = author_flair_text;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public int getUps() {
		return ups;
	}

	public void setUps(int ups) {
		this.ups = ups;
	}

	public int getNum_comments() {
		return num_comments;
	}

	public void setNum_comments(int num_comments) {
		this.num_comments = num_comments;
	}

	public String getNum_reports() {
		return num_reports;
	}

	public void setNum_reports(String num_reports) {
		this.num_reports = num_reports;
	}

	public String getDistinguished() {
		return distinguished;
	}

	public void setDistinguished(String distinguished) {
		this.distinguished = distinguished;
	}

	public int getDowns() {
		return downs;
	}

	public void setDowns(int downs) {
		this.downs = downs;
	}

	public boolean isSaved() {
		return saved;
	}

	public void setSaved(boolean saved) {
		this.saved = saved;
	}

	public String getCreated_utc() {
		return created_utc;
	}

	public void setCreated_utc(String created_utc) {
		this.created_utc = created_utc;
	}

	public String getCreated() {
		return created;
	}

	public void setCreated(String created) {
		this.created = created;
	}

	public Media getMedia() {
		return media;
	}

	public void setMedia(Media media) {
		this.media = media;
	}

	@Override
	public String toString() {
		return "Data [modhash=" + modhash + ", domain=" + domain
				+ ", banned_by=" + banned_by + ", name=" + name
				+ ", media_embed=" + media_embed + ", children="
				+ Arrays.toString(children) + ", subreddit=" + subreddit
				+ ", selftext_html=" + selftext_html + ", selftext=" + selftext
				+ ", likes=" + likes + ", link_flair_text=" + link_flair_text
				+ ", id=" + id + ", clicked=" + clicked + ", title=" + title
				+ ", score=" + score + ", approved_by=" + approved_by
				+ ", over_18=" + over_18 + ", hidden=" + hidden
				+ ", thumbnail=" + thumbnail + ", subreddit_id=" + subreddit_id
				+ ", edited=" + edited + ", link_flair_css_class="
				+ link_flair_css_class + ", author_flair_css_class="
				+ author_flair_css_class + ", author=" + author
				+ ", author_flair_text=" + author_flair_text + ", url=" + url
				+ ", created_utc=" + created_utc + ", permalink=" + permalink
				+ ", is_self=" + is_self + ", created=" + created + ", after="
				+ after + ", media=" + media + ", before=" + before + ", ups="
				+ ups + ", num_comments=" + num_comments + ", num_reports="
				+ num_reports + ", distinguished=" + distinguished + ", downs="
				+ downs + ", saved=" + saved + "]";
	}
	
}
