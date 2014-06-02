@(user: User, room:Room)(implicit r: RequestHeader)

$(document ).ready(function() {

    var WS = window['MozWebSocket'] ? MozWebSocket : WebSocket
    var gameSocket = new WS("@routes.Application.game(user.token,room.seq.getOrElse(0)).webSocketURL()");
    //chatSocket.replace("ws:","wss:");

    $.sendMessage = function() {
        gameSocket.send(JSON.stringify(
            {text: "haha"}
        ))
        //$("#talk").val('')
    }

    $.receiveEvent = function(event) {
        var data = JSON.parse(event.data)
        console.log(data);

        if(data.action == "join"){
            if("@room.owner.token" == data.token)   $.addUser(0,data);
            else                                    $.addUser(1,data);

            //$.addUser(data);

        }

        if(data.action == "addOwner") {
            if("@room.owner.token" != "@user.token")
            $.addOwner("@room.owner.token","@room.owner.name","@room.owner.pic_url");
        }

        if(data.action == "move"){
            if("@room.owner.token" == data.token)   $.setUserXY(0,data.token, data.x, data.y);
            else                                    $.setUserXY(1,data.token, data.x, data.y);

        }

        if(data.action == "jump"){

            if("@room.owner.token" == data.token)   $.jumpAction(0);
            else                                    $.jumpAction(1);

        }


        if(data.action == "shoot"){
            if("@room.owner.token" == data.token)   $.shooting(0);
            else                                    $.shooting(1);

        }

        if(data.action == "ready"){
            if("@room.owner.token" == "@user.token"){
                $(".btn-play").removeClass("disabled");
            }

        }

        if(data.action == "quit"){
            alert(data.name+" is quit!\nGame will exit");
            //alert($("#form2[id=token]")).val();
            $("#form2").submit();
        }

        if(data.action == "play"){
            ballMode = 'reset';
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

    gameSocket.onmessage = function(e) {
        $.receiveEvent(e);
    }

    $("#btn-exit-room").click(function() {
        //$.sendMessage();

        gameSocket.send(JSON.stringify(
            {
                seq_room: @room.seq
            }
        ))
    });

    $(".btn-play").click(function() {
        gameSocket.send(JSON.stringify(
            {
                seq_play_room: @room.seq,
                play:1
            }
        ))
        /*if(ballMode == 'ready')  ballMode = 'reset';
        else{
            $.jumpAction(1);
            $.jumpAction(0);
            //$.shooting(0);
            //$.shooting(1);
        }*/
    });

    $(".btn-ready").click(function() {
        gameSocket.send(JSON.stringify(
            {
                seq_ready_room: @room.seq,
                ready:1
            }
        ))
    });

});