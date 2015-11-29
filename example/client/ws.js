function WSManager() {

	var websocket;

	var _websocket = function() {
		this._ws = null;

		var _is_closed = false;
		var handlers = getWSHandlers();

		var _openHandler = function(evt) {
			console.log('WebSocket open');
		}

		var _messageHandler = function(evt) {
			console.log('WebSocket get message');
			var msg = evt.data;
			if (msg == 'health_check') {
				console.log('WebSocket connection is healthy.');
				return;
			}

			var data = JSON.parse(msg);
			$.each(handlers, function(index, handler) {
				if (('type' in handler) && handler.type == data.type) {
					handler.handle_message(data.message);
				}
			})
		}

		var close = function() {
			console.log('WebSocket closed');
            send_params({
                'action': 'unsubscribeAll'
            });
			_is_closed = true;
		}

		var health_ping = function() {
			if (_is_closed) {
				return;
			}
			console.log('send WebSocket health ping');
			send('health_check=1');
			setTimeout(health_ping, 1000 * 30);
		}

		var _closeHandler = function() {
			console.log('WebSocket closed');
            send_params({
                'action': 'unsubscribeAll'
            });
			_is_closed = true;
			_ws.close();
		}

		var _errorHandler = function(evt) {
			console.log('WebSocket error');
            show_error_toaster($("#record_pane_toaster"), "websocket链接出错，" + evt.data);
		}

		var connect = function(url) {
             _ws = new WebSocket(url);
            console.log("WebSocket connected to: " + curl);

            _ws.onopen = _openHandler;
            _ws.onmessage = _messageHandler;
            _ws.onclose = _closeHandler;
            _ws.onerror = _errorHandler;

            health_ping();
		}

		var wait_until_connected = function(action, total_duration) {
			if (_ws.readyState !==1) {
				if (total_duration <= 0) {
					console.log('Error when init WebSocket connection.');
					return;
				}
				setTimeout(function() {
					wait_until_connected(action, total_duration - 500);
				}, 500);
				return;
			}
			action();
		}

        var send = function(msg) {
        	if (! _ws) {
        		connect();
        	}
        	wait_until_connected(function(){
            	_ws.send(msg);
        	}, 5000);
        }

        var send_params = function(params) {
        	var msg = '';
        	for (var key in params) {
        		msg += key + '=' + params[key] + '&';
        	}
        	send(msg);
        }

        var register_handler = function(handler) {
        	handlers.push(handler);
        }

        var disregister_handler = function(type) {
        	var remove_index = [];
        	handlers.forEach(function(value, index) {
        		if (value.type == type) {
        			remove_index.push(index);
        		}
        	});
        	for (var i = remove_index.length - 1 ; i >= 0  ; --i) {
        		handlers.splice(remove_index[i], 1);
        	}
        }

        return {
        	connect: connect,
        	send: send,
        	send_params: send_params,
        	close: close,
        	register_handler: register_handler,
        	disregister_handler: disregister_handler
        }
	}

	var get_instance = function() {
		if (!websocket) {
			websocket = new _websocket();
		}
		return websocket;
	}

	return {
		get_instance: get_instance,
	}

}

function getWSHandlers() {

	var hello_handler = {
		
		// type must equal the type value of topic registered on server
		type: "1",

		// process the message
        handle_message: function(message) {
        	
        }
	}

	return [hello_handler];
}