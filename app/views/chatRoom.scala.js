@(user: User)(implicit r: RequestHeader)

$(document ).ready(function() {

    var WS = window['MozWebSocket'] ? MozWebSocket : WebSocket
    var chatSocket = new WS("@routes.Application.chat(user.token).webSocketURL()")
    //chatSocket.replace("ws:","wss:");

    $.sendMessage = function() {
        chatSocket.send(JSON.stringify(
            {text: $("#talk").val()}
        ))
        $("#talk").val('')
    }

    $.receiveEvent = function(event) {
        var data = JSON.parse(event.data)
        console.log(data)
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
    }

    chatSocket.onmessage = function(e) {
        $.receiveEvent(e);
    }

    $("#btn-make-room").click(function() {
        $.ajax(
                jsRoutes.controllers.Room.addRoom(
                    $("#mkroom_title").val(),
                    $("#mkroom_score").val(),
                    "@user.token"
                )
            ).done(function(data){
                console.log(data.seq);
                //location.href="/game?token=@user.token&seq_room="+data.seq
                $("#form2 input[name=seq_room]").val(data.seq);
                $("#form2").submit();

            } ).fail(function(e){
                console.log(e);
            });
    });

    $(".btn-join-room").click(function() {
        $("#form2 input[name=seq_room]").val($(this).data("roomSeq"));
        $("#form2").submit();
    });
});