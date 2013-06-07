This directory and it's subdirectories contain files expected to be static and thus allowed to be cached for long time (several months) by clients and public proxies. 

The whole directory (or parts of it) may get pre-cached by clients (e.g. using HTML5 offline mode, see http://www.w3.org/TR/html5/offline.html and http://www.html5rocks.com/en/features/offline)

Rules of thumb for this directory:
- For resources that are likely to be updated, include a version number in the filename for example "jquery-1.7.1.min.js" and add new files instead of changing content of existing ones. 
- Do your development (where file content regularly changes) in another directory without long-time caching, then move them here upon release.
- Files here are allowed to be cached in public proxies using a "Cache-Control: public" header so personal things like avatar images may need to be stored elsewhere for privacy reasons. 

If you mess up and really need to change a file here and do not want to wait for months for clients to update again, then you need to clear the cache of the clients (e.g. web browsers). 
