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
//Synth(\hydro2);

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
//Synth(\hydro3)
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
//Synth(\hydro4)
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
~playelm = {|elm,amp=0.0| ~elemsyn[elm] = ~mkelement.(~elem[elm],n:n).play(s,[\amp,amp]) };
~elems.do(~playelm);
~elemsyn.keys.do {|elm| ~elemsyn[elm].set(\amp,0.1)};
~elemsyn.keys.do {|elm| ~elemsyn[elm].set(\freq,1880.linrand)};//10000.linrand)};





/*
	~playelm.("C");
	~elemsyn["C"].set(\amp,0.1);
	~elemsyn["C"].set(\freq,880);
	//~elemsyn["C"].map(\amp, {LFSaw.kr(1)}.)
*/
