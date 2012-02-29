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

function createSeriesTree(seasons, options, now) {
	var list = $('<ul></ul>');

	$.each(seasons, function(key, value) {
		var tree = createSeasonTree(value.episodes, options, now);
		if (tree.children().size() == 0)
			return;

		var item = createTreeItem('Season ' + value.season, 'directory').append(tree);
		list.append(item);
	});

	return list.hide();
}

function createSeasonTree(episodes, options, now) {
	var list = $('<ul></ul>');

	$.each(episodes, function(key, value) {
		var state = getEpisodeState(value, now);

		if (!options.showSD && state === 'media_sd') return;
		if (!options.showHD && state === 'media_hd') return;
		if (!options.showUnknown && state === 'unknown') return;
		if (!options.showMissing && state === 'missing') return;
		if (!options.showUnaired && state === 'unaired') return;

		var item = createTreeItem(value.info.episode + ' ' + value.info.title, state);
		list.append(item);
	});

	return list.hide();
}

function getEpisodeState(episode, now) {
	if (typeof episode.file === 'undefined') {
		if (typeof epsiode.info.date !== 'undefined' && Date.parse(episode.info.date) > now)
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

function createMediaTree(options) {
	var list = $('#leftbox > ul.filetree').empty();

	var now = new Date().getTime();

	if (typeof options === 'undefined')
		options = {};

	if (typeof options.showSD === 'undefined') options.showSD = true;
	if (typeof options.showHD === 'undefined') options.showHD = true;
	if (typeof options.showUnknown === 'undefined') options.showUnknown = true;
	if (typeof options.showMissing === 'undefined') options.showMissing = true;
	if (typeof options.showUnaired === 'undefined') options.showUnaired = true;

	$.each(library, function(key, value) {
		var tree = createSeriesTree(value.seasons, options, now);
		if (tree.children().size() == 0)
			return;

		var item = createTreeItem(value.title, 'directory').append(tree);
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

			console.log(dest);
			// TODO
		},
	});
}

var websocket_uri = 'ws://' + window.location.hostname + ':8990/socket';

// Array holding information for all of our episodes
var library = [];

// Options used when creating the media tree
var options = {
	showSD: true,
	showHD: true,
	showUnkown: true,
	showMissing: true,
	showUnaired: true,
};

$(document).ready( function() {
	$.getJSON('library.json', function(d) {
		library = d;

		createMediaTree(options);

		$('#buttons > li > input:checkbox').change(function() {
			var key = $(this).attr('name');

			if (typeof options[key] === 'undefined')
				return;

			options[key] = $(this).prop('checked');

			createMediaTree(options);
		});
	});

	$.getJSON('extramedia.json', function(d) {
		var list = $('#extramedia > ul.filetree');

		$.each(d, function(key, value) {
			var state = getFileState(value);
			var item = createDraggableTreeItem(value.substring(1), state);
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
			var item = createDraggableTreeItem(value.substring(1), state);
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
