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

//s.quit;
s.boot;
s.options.numBuffers = 16000;
s.options.memSize = 655360;
s.freqscope;
s.plotTree;
s.scope;

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
	//hvp.postln;
	{
		|freq=1.0,amp=0.0|
		//hva.postln;
		Normalizer.ar(
			Mix.ar( SinOsc.ar(freq * hvp, mul: hva) ) / n,
		)!2 * amp
	}
};


~elems = ["H","Rb","Fe","He","Li","Be","B","C","O","N"];
~elem = Dictionary.new();
~elems.do {|elm| ~elem[elm] = ~csvtoinst.(elm++".csv")};

~elems.collect { |elm| ~elem[elm]["max"] };

n=200;
~elemsyn = Dictionary.new();
~playelm = {|elm,amp=0.0,freq=440.0| ~elemsyn[elm] = ~mkelement.(~elem[elm],n:n).play(s,[\amp,amp,\freq,freq]) };

~playelm = {|elm,amp=0.0,freq=440.0| ~elemsyn[elm] = ~mkelement.(~elem[elm],n:n).play(s,[\amp,amp,\freq,freq]) };

~playAnElm = {|elm,amp=0.0,freq=440.0,n=50| ~mkelement.(~elem[elm],n:n).play(s,[\amp,amp,\freq,freq]) };

SynthDef(\triramp,{
	|out=0,attackdur=20,decaydur=20|
	Out.kr(out,
		EnvGen.kr(
			Env.new([0,0.5,0],[attackdur,decaydur]),
			doneAction: 2
		)
	)
}).add;

//Env.new([0,0.5, 0], [1, 2],'linear').test.plot;
~elmAtkDecay = {
	|elm="H",attack=20.0,decay=20.0,freq=440.0|	
	Routine({
		var attackdur=attack,decaydur=decay,b,syn;
		syn = ~playAnElm.(elm,n: 100);
		s.sync;
		syn.set(\amp,0.0);
		s.sync;
		syn.set(\freq,freq);
		s.sync;
		b = Bus.control(s,1);
		b.set(0.0);
		syn.map(\amp,b);
		Synth(\triramp,[out: b, attackdur: attackdur, decaydur: decaydur]);
		(attackdur+decaydur+0.1).wait;
		syn.free;
		b.free;
	}).play;
};
// ~elmAtkDecay.("H");
// 
// ~elmAtkDecay.("O");
// 
// ~elmAtkDecay.("N");

//C8H10N4O2
// H3C - N  C-N-C  C=O C-C-C-N N-CH3 N-C=N N-C=C-N O-C-N-N
~caffeine = [
	["H","H","H","C"],
	["H","H","H","C","N"],
	["C","N","C"],
	["N","C","N"],
	["N","C","C","N","N"],
	["N","C","C","N","N","H"],
	["C","O","C","O"],
	["C","O","C","O","H"],
	["C","C","C","N"],
	["N","C","H","H","H"],
	["N","C","C","N","N"],
	["N","C","C","C","N"],
	["O","C","N","N"],
	["O","C","H","N","N"],
	["O","O","C"],
	["C","H","H","H"],
	["C","H"],
	["C","H","H"],
	["C","H","H","C","H"],
	["N","C","H","H","H"]
];



//~elems = ["H","Rb","Fe","He","Li","Be","B","C"];

~caffplay = {
	arg l,attack=20.0,decay=20.0,freq=120.0;
	var count = 1;
	l.postln;
	l.do {|elm|
		var ffreq = freq * count;
		count = count + 1;
		~elmAtkDecay.(elm,
			attack:attack, 
			decay:decay,
			freq: ffreq);
	};
};

//~caffplay.(~caffeine[0],attack:5,decay:5);

// ~caffeine[0].scramble

~caffplayroutine = Routine({
	120.do {
		var waitTime = 60.0 + (18.0.rand),
		    portion = 0.1 + (0.8.rand),
		    freq = (10.rand + 3) * 40.0,
		    l = ~caffeine.choose.scramble;
		[waitTime, freq, portion, l].postln;
		~caffplay.(l,
			attack:waitTime*portion, 
			decay:(1.1*waitTime)*(1.0-portion),
			freq:(20.rand + 3)*40.0);
		(0.25.rand + 0.25)*waitTime.wait;
	}
}).play;




~newH = Routine({
	var mult=20;
	((4*880)/mult).do {|x|
		~elmAtkDecay.("C",
			attack:40.0.rand, 
			decay:40.0.rand, 
			freq: (1.0 + (mult*x)));
		4.0.rand.wait;
	};
}).play;
~hrout = Routine({
	arg inval="H";
	var attackdur=20,elm,decaydur=20,b,syn = "H";
	elm = inval;
	~syn = ~playAnElm.(elm,n: 1000);
	s.sync;
	~syn.set(\freq,440.0);
	0.1.wait;
	~syn.set(\amp,0.0);
	0.1.wait;
	b = Bus.control(s,1);
	b.set(0.0);
	~syn.map(\amp,b);
	Synth(\triramp,[out: b, attackdur: attackdur, decaydur: decaydur]);
	//{  }.play;
	//{ Out.kr(b,Line.kr(0, end: 0.5, dur: attackdur, doneAction: 2)) }.play;
	//attackdur.wait;
	//{ Out.kr(b,Line.kr(start: 0.5, end: 0.0, dur: decaydur, doneAction: 2)) }.play;
	//decaydur.wait;
	(attackdur+decaydur+0.1).wait;
	b.set(0.0);
	0.1.wait;
	~syn.free;
});

	~elemsyn[elm].map(\amp,b);
	{ Out.kr(b,Line.kr(0, end: 0.5, dur: 6, doneAction: 2)) }.play;
}).play;

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
