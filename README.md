# MediaManager

MediaManager is a personal project for managing media. It handles automatically downloading desired TV shows and moving to the correct directory.

- Periodically checking [The TV Calendar](http://www.pogdesign.co.uk/cat/) to find out which shows to watch out for.
- Periodically checking various torrent sites (support for a limited of private sites) and matching new episodes against the list of desired episodes.
- Downloads any matched episodes by copying the .torrent file in to a configured watch directory.
- Watches a configured download directory for new media files or archives. Moves media files and extracts archives to a configured media directory.
- Periodically attempts to backfill missing episodes by searching various torrent sites (via scraping the search box, less efficient than the above RSS polling).
