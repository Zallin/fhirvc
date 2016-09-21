var options = {
    nodeMargin : 20,
    addColor : '#E1FAEA',
    delColor : '#FCE6E2'
}

function JSONtoDOM(rootEl, json){

    function type(obj){
	typeStr = Object.prototype.toString.call(obj)
	return typeStr.replace(/[\[\]]/g, '').split(' ')[1]
    }
    
    function inner(parent, json, color){
	var keys = Object.keys(json)
	keys.forEach(function (key){
	    var keyToColor = {'+' : options.addColor,
			      '-' : options.delColor}

	    if (key == '+' || key == '-'){
		var childColor = keyToColor[key],
		    toDraw = json[key];
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
		var child_margin = +parent.css('margin-left').replace('px', '') + options.nodeMargin + 'px';
		child.css('margin-left', child_margin)
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
	rootEl.empty()
	filediff_arr.forEach(function (json){
	    var ul = $(document.createElement('ul'))
	    compared = json['filenames']
	    ul.text(compared[0] +  ' compared to ' + compared[1]);
	    ul.addClass('file-comp')
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
