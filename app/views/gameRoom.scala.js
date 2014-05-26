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

        if(data.action == "jump"){
            @if(room.owner.token != user.token){
                $.jumpAction(1);
            }else{
                $.jumpAction(0);
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

    gameSocket.onmessage = function(e) {
        $.receiveEvent(e);
    }

    $("#btn-exit-room").click(function() {
        //$.sendMessage();

        gameSocket.send(JSON.stringify(
            {
                code: "removeRoom",
                seq_room:@room.seq
            }
        ))
    });

    $(".btn-play").click(function() {
        $.gameStart();
    })
});