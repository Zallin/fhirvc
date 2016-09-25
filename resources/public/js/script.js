$('form').submit(function (e){
    e.preventDefault()
    var form_data = $(this).serialize()
    $.get("/compare", form_data, function(filediff_arr){
	// TODO: check content type
	var rootEl = $('.root')
	rootEl.empty()
	filediff_arr.forEach(function (json){
	    prettyJson = $(document.createElement('pre'))
	    prettyJson.attr('id', 'json-viewer')
	    prettyJson.jsonViewer(json, {collapsed : true});
	    rootEl.append(prettyJson)
	});
	rootEl.css('display', 'block');
    })
	.error(function (err) {
	    alert('request error');
	    console.log(err);
	});
});
