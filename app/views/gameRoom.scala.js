@(user: User, room:Room)(implicit r: RequestHeader)
    var WS = window['MozWebSocket'] ? MozWebSocket : WebSocket
    var chatSocket = new WS("@routes.Application.game(user.token,room.seq.getOrElse(0)).webSocketURL()")


    $.sendMessage = function() {
        chatSocket.send(JSON.stringify(
            {text: "haha"}
        ))
        //$("#talk").val('')
    }

    $.receiveEvent = function(event) {
        var data = JSON.parse(event.data)
        console.log(data);

        if(data.action == "join"){
            $.addUser(data);
            @if(room.owner.token != user.token){
                $.addOwner("@room.owner.token","@room.owner.name","@room.owner.pic_url");
            }
        }

        if(data.action == "move"){
            @if(room.owner.token != user.token){
                $.setUserXY(1,data.token, data.x, data.y);
            }else{
                $.setUserXY(0,data.token, data.x, data.y);
            }
        }

        /*
        // Handle errors
        if(data.error) {
            chatSocket.close()
            $("#onError span").text(data.error)
            $("#onError").show()
            return
        } else {
            $("#onChat").show()
        }

        // Create the message element
        var el = $('<div class="message"><span></span><p></p></div>')
        $("span", el).text(data.user)
        $("p", el).text(data.message)
        $(el).addClass(data.kind)
        if(data.user == '@user.name') $(el).addClass('me')
        $('#messages').append(el)

        // Update the members list
        $("#members").html('')
        $(data.members).each(function() {
            var li = document.createElement('li');
            li.textContent = this;
            $("#members").append(li);
        })
        */
    }

    chatSocket.onmessage = $.receiveEvent

    $("#btn-exit-room").click(function() {
        $.sendMessage();
    });
