(sleep 2; mplayer --ao=jack:port=ffmpeg:name=fire ~/kdenlive/20140827-fire2.mp4) &
(sleep 2; jack_connect SuperCollider:out_1 ffmpeg:input_1) &
(sleep 2; jack_connect SuperCollider:out_2 ffmpeg:input_2) &
(sleep 4; jack_connect fire:out_0 system:playback_1) &
(sleep 4; jack_connect fire:out_1 system:playback_2) &
avconv -f jack -isync -ac 2 -i ffmpeg -f x11grab -r 30 -s 1920x1080 -i :0.0 -acodec pcm_s16le -vcodec libx264 -threads 0 -y bummer.mkv

