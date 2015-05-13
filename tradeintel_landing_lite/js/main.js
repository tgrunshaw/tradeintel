var dataUrl = "http://localhost/tradeintel2/webapp/categoryinfo.json";

function treeInit() {
	$('.tree').tree({
//			selectable: true,
//			dataUrl: dataUrl,
		dataUrl: 'js/categoryInfo.json'
//		dataFilter: function(raw) {
//			return convertData(raw);
//		}
	});
	$('.tree').bind('tree.click', updateReportInfo);
}

function getTreeData(categoryPath, callback) {
	$.getJSON(dataUrl + categoryPath, function(result) {
		var data = [convertData(result)];
		callback(data);
	});
}

function convertData(raw) {
	var data = {label: raw.name,
		path: raw.path,
		children: []};
	$.each(raw.childCategories, function(index, value) {
		data.children.push(convertData(this));
	});
	return data;
}

function updateReportInfo(event) {
	$('.order-column').css('visibility', 'visible');
	$('.category-path').html(prettify_path(event.node, true));
	$('.success-rate').text('' + event.node.successRate);
	$('.throughput').text('' + event.node.throughput);
	$('.meanprice').text('' + event.node.averagePrice);
	//$('.medal').text(calculateBadge(event.node.value)).removeClass().addClass(calculateBadge(event.node.value)).addClass('medal');
//	$('#input_7').val(prettify_path(event.node, false));
	$('#order-button').css('visibility', 'visible');
//	$('#your_form').attr('action', 'http://uri-for-button1.com');
	$('#order-button').attr('href', 'order.html?category=' + encodeURIComponent(prettify_path(event.node, false)));
}


function calculateBadge(value) {
	bronzeThreshold = 0;
	silverThreshold = 2000;
	goldThreshold = 10000;
	if(value < silverThreshold) {
		return 'bronze';
	} else if(value > silverThreshold && value < goldThreshold) {
		return 'silver';
	} else {
		return 'gold';
	}
}

	function prettify_path(node, colorify) {
		var divider = ' / ';
		if(colorify) {
			divider = "<span class='divider'> / </span>"; 
		}
		var curNode = node;
		var pretty_path = node.name;//node.name.replace("&", "&amp;");
		while (typeof curNode.parent.parent != "undefined" && curNode.parent.parent != null) {
			curNode = curNode.parent;
			pretty_path = curNode.name + divider + pretty_path;
		}
		return pretty_path;
	}