<html><body><div id="player"></div>
    <script type="text/javascript">
        var tag = document.createElement('script');
        tag.src = 'https://www.youtube.com/iframe_api';
        var firstScriptTag = document.getElementsByTagName('script')[0];
        firstScriptTag.parentNode.insertBefore(tag, firstScriptTag);
        var player;
        var quality = ['tiny', 'small', 'medium', 'large', 'hd720'];
        var lastState = -1;
        var i = 0;
        var timesBuffering = 0;
        var lastTime;
        function onYouTubeIframeAPIReady() {
            player = new YT.Player('player', {
                height: '100%',
                width: '100%',
                playerVars: {
                    autoplay: 1,
                    controls: 0,
                    showinfo: 0,
                    rel: 0
                },
                events: {
                    'onReady': onPlayerReady,
                    'onStateChange': onPlayerStateChange,
                    'onPlaybackQualityChange': onPlayerPlaybackQualityChange
                }
            });
        }
        function testEcho(message) {
            window.JSInterface.doEchoTest(message);
        }
        function onPlayerReady(event) {
            window.JSInterface.startBytes();
            player.loadVideoById({'videoId': '74k9vM6ELJk',
                'startSeconds': 40,
                'endSeconds': 50,
                'suggestedQuality': quality[i]});
            player.mute();
        }
        function onPlayerStateChange(event){
            testEcho(event.data);
            if (event.data == YT.PlayerState.ENDED && i < quality.length - 1) {
                if (lastState == YT.PlayerState.PAUSED){
                    window.JSInterface.onVideoEnded(quality[i], timesBuffering);
                    i = i + 1;
                }
                //testEcho(player.getPlaybackQuality());
                player.loadVideoById({'videoId': '74k9vM6ELJk',
                    'startSeconds': 40,
                    'endSeconds': 50,
                    'suggestedQuality': quality[i]});
            }
            else if (event.data == YT.PlayerState.UNSTARTED){
                timesBuffering = 0;
            }

            else if (event.data == YT.PlayerState.BUFFERING){
                //timesBuffering++;
                lastTime = Date.now();
            }
            else if (lastState == YT.PlayerState.BUFFERING){
                timesBuffering += Date.now() - lastTime
            }

            lastState = event.data;
        }
        function onPlayerPlaybackQualityChange(event){
            if (i>0 && event.data == quality[i-1]){
                player.stopVideo();
                window.JSInterface.onVideoTestFinish();
                return;
            }
            window.JSInterface.makeToast(event.data);
            testEcho(event.data);
        }
    </script>
</body></html>