function createTreeItem(title, type) {
	return $('<li></li>').addClass(type).append($('<a></a>').attr('href', '#').attr('title', ' ' + title + ' ').text(title));
}

function createDraggableTreeItem(title, type) {
	var item = createTreeItem(title, type);

	item.draggable({
		revert: 'invalid',
		helper: 'clone',
		cursor: 'move',
	});

	return item;
}

function createSeriesTree(seasons, now) {
	var list = $('<ul></ul>');

	var seriesState = null;

	$.each(seasons, function(key, value) {
		var tree = createSeasonTree(value.episodes, now);
		if (tree.children().size() == 0)
			return;

		var state = tree.attr('rel');
		tree.removeAttr('rel');

		if (seriesState == null)
			seriesState = state;
		else if (seriesState != state)
			seriesState = 'mixed';

		var item = createTreeItem('Season ' + value.season, 'directory').attr('rel', state).append(tree);
		list.append(item);
	});

	list.attr('rel', seriesState);

	return list.hide();
}

function createSeasonTree(episodes, now) {
	var list = $('<ul></ul>');

	var seasonState = null;

	$.each(episodes, function(key, value) {
		var state = getEpisodeState(value, now);

		if (seasonState == null)
			seasonState = state;
		else if (seasonState != state)
			seasonState = 'mixed';

		var item = createTreeItem(value.info.episode + ' ' + value.info.title, 'file').attr('rel', state);
		list.append(item);
	});

	list.attr('rel', seasonState);

	return list.hide();
}

function getEpisodeState(episode, now) {
	if (typeof episode.file === 'undefined') {
		if (typeof episode.info.date === 'undefined' || Date.parse(episode.info.date) > now)
			return 'unaired';

		return 'missing';
	}

	return fileExtToType(episode.file.fileExt);
}

function getFileState(filename) {
	var ext = filename.split('.').pop();
	return fileExtToType(ext);
}

function fileExtToType(ext) {
	switch (ext) {
		case 'mkv': {
			return 'media_hd';
		}
		
		case 'avi': {
			return 'media_sd';
		}
		
		default: {
			return 'unknown';
		}
	}
}

function log(msg, type) {
	var time = new Date();

	time = $('<span></span>').addClass('date').text(time.format('dd/mm/yy HH:MM:ss'));
	msg = $('<span></span>').text(msg);

	$('#log').prepend($('<p></p>').addClass(type).append(time).append(msg));
}

function createMediaTree() {
	var list = $('#leftbox > ul.filetree').empty();

	var now = new Date().getTime();

	$.each(library, function(key, value) {
		var tree = createSeriesTree(value.seasons, now);
		if (tree.children().size() == 0)
			return;

		var state = tree.attr('rel');
		tree.removeAttr('rel');

		var item = createTreeItem(value.title, 'directory').attr('rel', state).append(tree);
		list.append(item);
	});

	list.find('a').click(function() {
		$(this).parent().children('ul').slideToggle();
		return false;
	});

	// Find all missing episodes, and create drop targets
	list.find('li.missing a').droppable({
		accept: '#extramedia > ul > li, #extradls > ul > li',
		drop: function(event, ui) {
			var src = $(ui.draggable);
			var dest = $(this);

			console.log(src);
			console.log(dest);
			// TODO
		},
	});
}

function processTreeItem(item, options) {
	var state = item.attr('rel');
	var visibleChildren = 0;

	// If it's mixed then process it's children individually
	if (state == 'mixed') {
		var children = item.children('ul').children('li');
		children.each(function() {
			visibleChildren += processTreeItem($(this), options);
		});

		// Make us visible if we have children, or hidden otherwise
		item.toggle(visibleChildren > 0);
	}
	// Otherwise process this item
	else {
		var show = options[state] ? true : false;
		item.toggle(show);
	
		if (show)
			visibleChildren++;
	}

	return visibleChildren;
}

var websocket_uri = 'ws://' + window.location.hostname + ':8990/socket';

// Array holding information for all of our episodes
var library = [];

$(document).ready( function() {
	$.getJSON('library.json', function(d) {
		library = d;

		createMediaTree();

		$('#buttons > li > input:checkbox').change(function() {
			var options = {
				media_sd: $('#showSD').prop('checked'),
				media_hd: $('#showHD').prop('checked'),
				unknown: $('#showUnknown').prop('checked'),
				missing: $('#showMissing').prop('checked'),
				unaired: $('#showUnaired').prop('checked'),
			};

			$('#leftbox > ul.filetree > li').each(function() {
				processTreeItem($(this), options);
			});
		});
	});

	$.getJSON('extramedia.json', function(d) {
		var list = $('#extramedia > ul.filetree');

		$.each(d, function(key, value) {
			var state = getFileState(value);
			var item = createDraggableTreeItem(value.substring(1), 'file').attr('rel', state);
			list.append(item);
		});
		
		list.find('a').click(function() {
			return false;
		})
	});

	$.getJSON('extradls.json', function(d) {
		var list = $('#extradls > ul.filetree');

		$.each(d, function(key, value) {
			var state = getFileState(value);
			var item = createDraggableTreeItem(value.substring(1), 'file').attr('rel', state);
			list.append(item);
		});

		list.find('a').click(function() {
			return false;
		})
	});

	var ws = new WebSocket(websocket_uri);
	ws.onopen = function() {
		log('Connection opened', 'connection');
	}

	ws.onmessage = function(e) {
		var msg = JSON.parse(e.data);
		switch (msg.type) {
			case 'LogMessage': {
				log(msg.value.message, msg.value.type.toLowerCase());
				break;
			}
		}
	};

	ws.onclose = function() {
		log('Connection closed', 'connection');
	}
});
