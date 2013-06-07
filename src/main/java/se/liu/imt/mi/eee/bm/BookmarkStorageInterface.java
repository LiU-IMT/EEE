package se.liu.imt.mi.eee.bm;

import java.util.List;


public interface BookmarkStorageInterface {
	
	/**
	 * The implementing class is expected to assign ID
	 * @param committer
	 * @param bookmark
	 * @return the ID of the created bookmark should be returned
	 * 
	 * TODO: Consider if id suggestions from client should be allowed and if so auto assign on id=null only (throw error on conflict)
	 */
	public String createAndStoreBookmark(String committer, Bookmark bookmark);
	
	/**
	 * Update title etc for existing bookmarks 
	 * Note that it should not be possible to change ID or comitter
	 */
	public void updateBookmark(String committer, String bookmarkID, Bookmark bookmark);
	
	
	/**
	 * @param bookmark_id
	 * @return gets bookmarks if they exist (included deactivated ones), otherwise returns null
	 */
	public Bookmark getBookmark(String bookmark_id);

	/**
	 * @param user_id
	 * @return gets bookmarks for a user if any exist (including deactivated ones), otherwise return null if user has no bookmarks.
	 */
	public List<Bookmark> listUserBookmarks(String user_id);

	/**
	 * A bookmark can be inactivated by the one who created it. Because of audit reasons
	 * it should not be physically deleted, but {@link #getBookmark(String)} should return
	 * null if the bookmark is deactivated.
	 * @param committer
	 * @param bookmark_id
	 * @return returns the deactivated bookmark object after it has been changed in storage
	 */
	public Bookmark deactivateBookmark(String committer, String bookmark_id);

}
