function JSONtoDOM(rootEl, json){

    function type(obj){
	typeStr = Object.prototype.toString.call(obj)
	return typeStr.replace(/[\[\]]/g, '').split(' ')[1]
    }
    
    function inner(parent, json, color){
	var keys = Object.keys(json)
	keys.forEach(function (key){
	    var keyToColor = {'+' : 'green',
			      '-' : 'red'}

	    if (key == '+' || key == '-'){
		var childColor = keyToColor[key],
		    toDraw = json[key];
		console.log(childColor);
		inner(parent, toDraw, childColor);
	    }
	    else {
		var child,
		    valueType = type(json[key]);
		
		if (valueType == 'Array' || valueType == 'Object'){
		    child = $(document.createElement('ul'))
		    child.text(key)
		    inner(child, json[key], null)
		}
		else {
		    child = $(document.createElement('li'))
		    child.text(key + ' : ' + json[key])
		}
		if (color) {
		    child.css('background-color', color)
		}

		parent.append(child)
	    }
	
	});
    }
    inner(rootEl, json, null)
}

$('form').submit(function (e){
    e.preventDefault()
    var form_data = $(this).serialize()
    $.get("/compare", form_data, function(filediff_arr){
	// TODO: check content type
	var rootEl = $('.root')
	filediff_arr.forEach(function (json){
	    var ul = $(document.createElement('ul'))
	    ul.text("Files: " + json["filenames"].join(" "))
	    JSONtoDOM(ul, json["difference"])
	    rootEl.append(ul);
	});
	rootEl.css('display', 'block');
    })
	.error(function (err) {
	    alert('request error');
	    console.log(err);
	});
});
