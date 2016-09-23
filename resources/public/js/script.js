var options = {
    nodeMargin : 20,
    addColor : '#E1FAEA',
    removedColor : '#FCE6E2'
}

function type(obj){
    typeStr = Object.prototype.toString.call(obj)
    return typeStr.replace(/[\[\]]/g, '').split(' ')[1]
}

function isCompound(el){
    return type(el) == 'Object' || type(el) == 'Array';
}

function childMargin(parentMargin){
    return parentMargin.replace('px', '') + options.nodeMargin + 'px';
}

function objToDOM(json){
    var root = $(document.createElement('ul')),
	keys = Object.keys(json);
    
    keys.forEach(function (key){
	var val = json[key],
	    child;
	if (isCompound(val)){
	    child = JSONtoDOM(val)
	    child.prepend(key)
	} else {
	    child = $(document.createElement('li'));
	    child.text(key + ' : ' + val)
	}
	if (key == 'removed'){
	    child.css('background-color', options.removedColor)
	}
	if (key == 'added'){
	    child.css('background-color', options.addColor)
	}
	var chmrg = childMargin(root.css('margin-left'));
	child.css('margin-left', chmrg)
	root.append(child)
    });
    return root
}

function arrayToDOM(json){
    var root = $(document.createElement('ul'));
    json.forEach(function (el){
	var child;
	if (isCompound(el)){
	    child = JSONtoDOM(el)
	} else {
	    child = $(document.createElement('li'));
	    child.text(el)	
	}
	var chmrg = childMargin(root.css('margin-left'))
	child.css('margin-left', chmrg)
	root.append(child)
    });
    return root
}

function JSONtoDOM(json){
    var res;

    if (type(json) == 'Array'){
	res = arrayToDOM(json)
    }
    else if (type(json) == 'Object'){
	res = objToDOM(json)
    }
    return res
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
	    var el = JSONtoDOM(json["difference"])
	    ul.append(el)
	    rootEl.append(ul);
	});
	rootEl.css('display', 'block');
    })
	.error(function (err) {
	    alert('request error');
	    console.log(err);
	});
});
