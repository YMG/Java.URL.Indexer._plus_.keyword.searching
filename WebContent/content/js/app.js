"use strict";

$(function ()
{

	var isRunning = false;
	var sections = {
		config_ui: $('.config'),
		web_ui: $('.web'),
		db_ui: $('.db'),
		db_request: $('#dbconfig_form'),
		update_status: function ()
		{
			if (localStorage['jobs'])
			{
				isRunning = true;
				$.ajax(
				{
					url: '/wtquery',
					type: 'GET',
					cache: false,
					timeout: 8000,
					dataType: 'json',
					data:
					{
						jobs: localStorage['jobs']
					},
					error: function (e) {},
					success: function (resp)
					{
						if (resp.countedTasks == 0)
						{
							localStorage.clear();
							$('#task_count').html("0");
							isRunning = false;
						}
						else
						{
							$('#task_count').html(resp.countedTasks);
							setTimeout(function ()
							{
								sections.update_status();
							}, 5000);
						}
					}
				});
			}
		}
	};

	(function ()
	{
		sections.update_status();
	})();

	$('.usr_pass_diable').change(function ()
	{
		if (this.checked)
		{
			$('input[name=dbpassword]').prop("disabled", true);
			$('input[name=dbuser]').prop("disabled", true);
			$('#db_config_fields').addClass('hide');
		}
		else
		{
			$('input[name=dbpassword]').prop("disabled", false);
			$('input[name=dbuser]').prop("disabled", false);
			$('#db_config_fields').removeClass('hide');
		}
	});

	function alert_success_show(msg)
	{

		var notification = document.createElement("div");
		notification.classList.add('uk-alert');
		notification.classList.add('uk-alert-large');
		notification.classList.add('uk-alert-success');
		notification.classList.add('uk-animation-slide-bottom');
		notification.setAttribute('data-uk-alert', '');

		var closebutton = document.createElement('a');
		closebutton.classList.add('uk-alert-close');
		closebutton.classList.add('uk-close');
		closebutton.setAttribute('href', '');

		var fixedMsg = document.createElement('p');
		fixedMsg.innerHTML = "your job added to queue with id: ";

		var incMsg = document.createElement('h2');
		incMsg.innerHTML = msg;

		notification.appendChild(closebutton);
		notification.appendChild(fixedMsg);
		notification.appendChild(incMsg);

		$('#alerts').html(notification);
	}

	function alert_message_show(msg)
	{
		var notification = document.createElement("div");
		notification.classList.add('uk-alert');
		notification.classList.add('uk-alert-large');
		notification.classList.add('uk-animation-slide-bottom');
		notification.setAttribute('data-uk-alert', '');

		var closebutton = document.createElement('a');
		closebutton.classList.add('uk-alert-close');
		closebutton.classList.add('uk-close');
		closebutton.setAttribute('href', '');

		var incMsg = document.createElement('h2');
		incMsg.innerHTML = msg;

		notification.appendChild(closebutton);
		notification.appendChild(incMsg);

		$('#alerts').html(notification);
	}

	function alert_danger_show(msg)
	{
		var notification = document.createElement("div");
		notification.classList.add('uk-alert');
		notification.classList.add('uk-alert-large');
		notification.classList.add('uk-alert-danger');
		notification.classList.add('uk-animation-slide-bottom');
		notification.setAttribute('data-uk-alert', '');

		var closebutton = document.createElement('a');
		closebutton.classList.add('uk-alert-close');
		closebutton.classList.add('uk-close');
		closebutton.setAttribute('href', '');

		var incMsg = document.createElement('h2');
		incMsg.innerHTML = msg;

		notification.appendChild(closebutton);
		notification.appendChild(incMsg);

		$('#alerts').html(notification);
	}


	var opts = {
		lines: 11, // The number of lines to draw
		length: 21, // The length of each line
		width: 7, // The line thickness
		radius: 26, // The radius of the inner circle
		corners: 0.6, // Corner roundness (0..1)
		rotate: 0, // The rotation offset
		direction: 1, // 1: clockwise, -1: counterclockwise
		color: '#fff', // #rgb or #rrggbb or array of colors
		speed: 0.7, // Rounds per second
		trail: 56, // Afterglow percentage
		shadow: true, // Whether to render a shadow
		hwaccel: true, // Whether to use hardware acceleration
		className: 'spinner', // The CSS class to assign to the spinner
		zIndex: 2e9, // The z-index (defaults to 2000000000)
		top: 'auto', // Top position relative to parent in px
		left: 'auto' // Left position relative to parent in px
	};
	var target = document.getElementById('msg');
	var spinner = new Spinner(opts).spin(target);



	var dbfrm = $('#dbconfig_form').validate(
	{
		rules:
		{
			url:
			{
				required: true
			},
			dbaddress:
			{
				required: true
			},
			dbuser:
			{
				required: true
			},
			dbpassword:
			{
				required: true
			},
		},
		errorPlacement: function (e, r) {},
		highlight: function (element)
		{
			$(element).removeClass('uk-form-success').addClass('uk-form-danger');
		},
		unhighlight: function (element)
		{
			$(element).removeClass('uk-form-danger').addClass('uk-form-success');
		},
		submitHandler: function (form)
		{

			$.ajax(
			{
				url: '/wtquery',
				type: 'POST',
				cache: false,
				timeout: 8000,
				dataType: 'json',
				data: $('#dbconfig_form').serialize(),
				beforeSend: function ()
				{
					$('#loading').removeClass('hide');
				},
				error: function (e)
				{
					$('#loading').addClass('hide');
					alert_danger_show("Failed to connect");
				},
				success: function (resp)
				{
					$('#loading').addClass('hide');
					var requestedTasks = [];
					if (localStorage['jobs'])
					{
						requestedTasks = JSON.parse(localStorage['jobs']);
					}

					if (resp.success)
					{
						alert_success_show(resp.success);
						requestedTasks.push(resp.success);
						var taskList = JSON.stringify(requestedTasks);
						localStorage['jobs'] = taskList;
						if (!isRunning)
						{
							sections.update_status();
						}
					}
					else
					{
						alert_danger_show(resp.error);
					}
					$(':input').not(':checkbox').val('');
				}
			});

		}
	});

	$('#canceldb').click(function ()
	{
		$("#dbconfig_form")[0].reset();
		$('.usr_pass_diable').change();
	});


	var qfrm = $('#query_form').validate(
	{
		rules:
		{
			keywords:
			{
				required: true
			}
		},
		errorPlacement: function (e, r) {},
		highlight: function (element)
		{
			$(element).removeClass('uk-form-success').addClass('uk-form-danger');
		},
		unhighlight: function (element)
		{
			$(element).removeClass('uk-form-danger').addClass('uk-form-success');
		},
		submitHandler: function (form)
		{

			$.ajax(
			{
				url: '/wtquery',
				type: 'GET',
				dataType: 'json',
				beforeSend: function ()
				{
					$('#loading').removeClass('hide');
				},
				data: $('#query_form').serialize(),
				error: function (e)
				{
					$('#loading').addClass('hide');
					alert_danger_show("Failed to connect");
				},
				success: function (resp)
				{
					$('#loading').addClass('hide');
					if (resp.failed)
					{
						alert_danger_show(resp.failed);
					}
					else
					{
						if (resp.match == "null")
						{
							alert_message_show("no results found");
						}
						else
						{

							var parsed = $.parseJSON(resp.match);
							var template = document.getElementById('keyword_result').innerHTML;
							var output = Mustache.render(template, parsed);
							document.getElementById('results').innerHTML = output;
						}
					}
					$(':input').not(':checkbox').val('');
				}
			});

		}
	});

});