// 
// Copyright 2014 Abram Hindle
// 
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
// 
//     http://www.apache.org/licenses/LICENSE-2.0
// 
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
// 

s.boot;
s.freqscope;
s.plotTree;

SynthDef(\hydro1, {
	var n = (2..10);
	Out.ar(0,
		(n.collect {arg i; SinOsc.ar( (1 - (1/(i*i))) * 440 )}).sum
	)
}).add;
//Synth(\hydro1);

//pleasent sound
SynthDef(\hydro2, {
	|out=0,amp=1.0,freq=440.0|
	var nsize,n = (2..10);
	nsize = n.size;
	Out.ar(0,
		amp * 
		(
			n.collect {arg i; 
				SinOsc.ar( (1.0 - (1.0/(i*i))) * freq )
			}).sum / nsize
	)
}).add;
//~hydro2 = Synth(\hydro2);

// loud and noisey
SynthDef(\hydro2clip, {
	|out=0,amp=1.0|
	var nsize,n = (2..10);
	nsize = n.size;
	Out.ar(0,
		Clip.ar(
		amp * 
		(
			n.collect {arg i; 
				SinOsc.ar( (1 - (1/(i*i))) * 440 )
			}).sum // nsize
		)
	)
}).add;

SynthDef(\hydro3, {
	|out=0,amp=1.0,freq=440|
	var nsize,n = (2..10);
	nsize = n.size;
	Out.ar(0,
		amp * 
		(
			n.collect {arg i; 
				SinOsc.ar( (1.0 - (1/(i*i))) * freq ) +
				SinOsc.ar( ((1/4) - (1/((i+1)*(i+1)))) * freq)
			}).sum / (2 * nsize)
	)
}).add;
//~hydro3 = Synth(\hydro3)
SynthDef(\hydro4, {
	|out=0,amp=1.0,freq=440|
	var nsize,n = (2..10);
	nsize = n.size;
	Out.ar(0,
		amp * 
		(
			n.collect {arg i; 
				SinOsc.ar( (1.0 - (1/(i*i))) * 2*freq ) +
				SinOsc.ar( (1.0 - (1/(i*i))) * freq ) +
				SinOsc.ar( ((1/4) - (1/((i+1)*(i+1)))) * freq)
			}).sum / (3 * nsize)
	)
}).add;
//Synth(\hydro3)
//~hydro4 = Synth(\hydro4)
//~hydro4.set(\amp,0.3)
//~hydro3.set(\amp,0.3)
//~hydro2.set(\amp,0.3)
~truemax = 2701243;
~csvtoinst = {
	arg filename;
	var rows,fmin,fmax,out;
	rows = CSVFileReader.read(filename); 
	rows.removeAt(0);
	rows = rows.collect {|v| v[1] = v[1].asFloat; v[2] = v[2].asFloat; };
	out = Dictionary.new();
	out["rows"] = rows;
	out["max"] = rows[rows.maxIndex{|v|v[2]}].[2];
	out["min"] = rows[rows.minIndex{|v|v[2]}].[2];
	out["fmax"] = rows[rows.maxIndex{|v|v[1]}].[1];
	out["fmin"] = rows[rows.minIndex{|v|v[1]}].[1];

	out
};

~mkelement = {
	arg element, bfreq=1.0, n=10;
	var hv,hvp,hva,emin,emax;
	emin = (element["min"]);
	emax = (element["max"]);
	hv = element["rows"][{element["rows"].size.rand}!n];
	hvp = hv.collect {|v| v[1] * bfreq };
	hva = hv.collect {|v| (v[2])/(~truemax) };	
	hvp.postln;
	{
		|freq=1.0,amp=1.0|
		hva.postln;
		Normalizer.ar(
			Mix.ar( SinOsc.ar(freq * hvp, mul: hva) ) / n,
		) * amp
	}
};


~elems = ["H","Rb","Fe","He","Li","Be","B","C"];
~elem = Dictionary.new();
~elems.do {|elm| ~elem[elm] = ~csvtoinst.(elm++".csv")};

~elems.collect { |elm| ~elem[elm]["max"] };

n=200;
~elemsyn = Dictionary.new();
~playelm = {|elm,amp=0.0| ~elemsyn[elm] = ~mkelement.(~elem[elm],n:n).play(s,[\amp,amp,\freq,440.0]) };
~elems.do(~playelm);
// wait a little
~elemsyn.keys.do {|elm| ~elemsyn[elm].set(\amp,0.2.linrand)};
~elemsyn.keys.do {|elm| ~elemsyn[elm].set(\freq,1000.linrand)};

~elemsyn["Fe"].set(\freq,2000)




/*
	~playelm.("C");
	~elemsyn["C"].set(\amp,0.1);
	~elemsyn["C"].set(\freq,440);
	//~elemsyn["C"].map(\amp, {LFSaw.kr(1)}.)


*/

/* 

~c2 = ~mkelement.(~elem["C"],n:n).play(s,[\amp,0.0]);
~c2.set(\amp,0.1);
~c2.set(\freq,880.0);

~c3 = ~mkelement.(~elem["C"],n:n).play(s,[\amp,0.0,\freq,220]);
~c3.set(\amp,0.3);
~c3.set(\freq,220.0);

*/

~free = {|synthn|
	s.sendBundle(nil,Synth.basicNew("default",s,synthn).freeMsg)
};

// ~free.(1003)

/*
	~h3 = ~mkelement.(~elem["H"],n:n).play(s,[\amp,0.0,\freq,220]);
	~h3.set(\amp,0.3);
	~h3.set(\freq,880.0);
	~h3.asString
*/

~mkslider = {
	|synth,params,name=""|
	var window,cv;
	window = Window(name++synth.asString, Rect(200, 200, 200, 200), false).front;
	cv = View(window);
	cv.minSize_(Size(199,199));
	cv.layout_(VLayout());	
	params.do {|p|
		var ss = Slider();//, Rect(50, 50, 50, 10))
		ss.action_({|slider| synth.set(p[0], [p[1], p[2]].asSpec.map(slider.value))});
		ss.orientation(\horizontal);
		cv.layout.add(ss,0,\topleft);
	};
};
//~mkslider.(~h3,[[\freq,20,2000],[\amp,0,1.0]])

~elemsyn.keys.do {|elm| ~mkslider.(~elemsyn[elm],[[\freq,20,2000],[\amp,0,1.0]], name: elm)}

~rs = Dictionary.new();

(
~rs["B"] = Routine({
	var pbf = Pbrown(0,1,0.05).asStream;
	var pba = Pbrown(0,1,0.05).asStream;
	loop {
		var pbn = 40 + (pbf.next * 1000.0);		
		~elemsyn["B"].set(\freq,pbn);
		~elemsyn["B"].set(\amp,pba.next*1.0);
		pbn.postln;
		2.0.rand.wait;
	}
}).play;
)
~rs.keys.do { |key| ~rs[key].stop; }
~rs["Cfreq"].stop;
~f.free;
~ff.stop;
~elemsyn["Fe"].get(\amp,{|x| x.postln});


SynthDef(\cnoise,
	{
		|out=0,base=440,range=4,freq=1| 
		Out.kr(out,LFNoise1.kr(freq,range,base))
	}
).add;
~freqs = Dictionary.new();
~freqnoise = Dictionary.new();
~freqit = {|elm|
	~freqs[elm] = Bus.control(s,1);
	~freqnoise[elm] = Synth(\cnoise,[\out,~freqs[elm],\freq,1,\range,200,\base,440]);
	~elemsyn[elm].map(\freq,~freqs[elm])
};

~freqs["Fe"] = Bus.control(s,1);
~freqs["Fe"].set(440);
~freqs["H"] = Bus.control(s,1);
~freqs["H"].set(440);
~cnoise = Synth(\cnoise,[\out,~freqs["Fe"]]);
~cnoiseh = Synth(\cnoise,[\out,~freqs["H"]]);

~elemsyn["Fe"].map(\freq,~freqs["Fe"])
~elemsyn["H"].map(\freq,~freqs["H"])

~amps = Dictionary.new();
~ampnoise = Dictionary.new();
~ampit = {|elm|
	~amps[elm] = Bus.control(s,1);
	~ampnoise[elm] = Synth(\cnoise,[\out,~freqs[elm],\freq,3,\range,1,\base,0]);
	~elemsyn[elm].map(\amp,~amps[elm])
};
~ampit.("C")
~freqit.("C")
~free.(1010)
~free.(1009)
~free.(1008)
~freqnoise["C"].set(\base,800)
