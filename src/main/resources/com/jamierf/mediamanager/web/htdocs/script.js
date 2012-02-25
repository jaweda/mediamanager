$(document).ready( function() {
	var options = {
		root: '/',
		script: 'filetree.html',
		multiFolder: true,
	};

	$('#leftbox').fileTree(options, function(file) {
		alert(file);
	});
});
