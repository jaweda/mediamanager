$(document).ready( function() {
	$('#leftbox').fileTree({
		root: '/',
		script: 'filetree.html',
		multiFolder: true,
	}, function(file) {
		return false;
	});
	
	function log(msg, type) {
		var time = new Date();

		time = $('<span class="date"></span>').text(time.format('dd/mm/yy HH:MM:ss'));
		msg = $('<span></span>').text(msg);

		$('#log').prepend($('<p class="' + type + '"></p>').append(time).append(msg));
	}
	
	var ws = new WebSocket('ws://' + window.location.host + '/socket');
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
