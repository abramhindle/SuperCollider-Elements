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

// bummer ideas:
//  - palette of chemicals
//  - global reverb or amp
//    - 19 sustain then 1 minute decay
//    - get rid of that long one?
//    - or run a routine of 5 minutes of C, H, O, N
//  - print the chemical somehow
//  - practice again
//  - try on laptop with projector

//s.quit;
s.options.numBuffers = 16000;
s.options.memSize = 655360;
s.boot;
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
		|freq=1.0,amp=0.0,attack=20,decay=20|
		//hva.postln;
		Normalizer.ar(
			Mix.ar( SinOsc.ar(freq * hvp, mul: hva) ) / n,
		)!2 * EnvGen.kr(
			Env.new([0,0.5,0],[attack,decay]),
			doneAction: 2
		)!2 * amp
	}
};


~elems = ["H","Rb","Fe","He","Li","Be","B","C","O","N","Cl","S"];
~elem = Dictionary.new();
~elems.do {|elm| ~elem[elm] = ~csvtoinst.(elm++".csv")};

~elems.collect { |elm| ~elem[elm]["max"] };

n=200;
~elemsyn = Dictionary.new();

~playAnElm = {|elm,fadein=0.2,amp=0.0,freq=440.0,n=50| ~mkelement.(~elem[elm],n:n).play(s,fadeTime:fadein,args:[\amp,amp,\freq,freq]) };

~playelm = {|elm,amp=0.0,freq=440.0,attack=20.0,decay=20.0,n=100| ~mkelement.(~elem[elm],n:n).play(s,args:[\amp,amp,\freq,freq,\attack,attack,\decay,decay]) };
//~playelm.("H",amp: 0.2, freq: 440)
~playelm.("H",amp: 0.2, freq: 1440, attack:1.0, decay: 1.0)


/*
x = ~playAnElm.("H",fadein:10,amp:1.0,freq:1440.0,n:150)
x.release(1.0)

x = ~playAnElm.("H",1.0,44440.0,n:150)
x.set(\amp,0.1)
x.set(\freq,4440.0)
x.get(\freq,{|x| x.postln})
x.get(\amp,{|x| x.postln})
h = ~mkelement.(~elem["H"],n:150)
y = h.play(s,args:[\amp,1.0,\freq,440])
y= h.(amp:1.0, freq: 440.0)
y.get(\freq,{|x| x.postln})
y.get(\amp,{|x| x.postln})
h
y.defName
*/

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
~elmplayer = {
	|elm="H",attack=20.0,decay=20.0,freq=440.0,n=100,amp=0.1|	
	~playelm.(elm: elm, amp:amp, freq: freq, attack: attack, decay: decay, n: n)
};
//~elmplayer.("H",attack:20.0,decay:20.0,freq:440.0,n:100,amp:0.5)
~elmAtkDecay = {
	|elm="H",attack=20.0,decay=20.0,freq=440.0,n=100|	
	Routine({
		var attackdur=attack,decaydur=decay,b,syn;
		syn = ~playAnElm.(elm,n: n);
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
	l.collect {|elm|
		var ffreq = freq * count;
		count = count + 1;
		// ~elmAtkDecay.(elm,
		~elmplayer.(elm,
			attack:attack, 
			decay:decay,
			freq: ffreq, n: 50);
	};
};
~elmplayer.(elm:"H",attack:10,decay:10,freq:1000,n:50)
//~caffplay.(["H","H","H"],freq:1100,attack:5,decay:5)

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
});

// 6C10H15O7 -> C50H10O 10CH2O
// CH2O + O2 -> H2O CO2 CO C N2

// carbon monoxide 	80-370
// methane 	14-25
// VOCs* (C2-C7) 	7-27
// aldehydes 	0.6-5.4
// substituted furans 	0.15-1.7
// benzene 	0.6-4.0
// alkyl benzenes 	1-6
// acetic acid 	1.8-2.4
// formic acid 	0.06-0.08
// nitrogen oxides 	0.2-0.9
// sulfur dioxide 	0.16-0.24
// methyl chloride 	0.01-0.04
// napthalene 	0.24-1.6
// substituted napthalenes 	0.3-2.1
// oxygenated monoaromatics 	1-7
// total particle mass 	7-30
// particulate organic carbon 	2-20
// oxygenated PAHs 	0.15-1
// Individual PAHs 	10-5-10-2

~wood = [
	["C","H","O"],
	["H","H","O"],
	["C","H","H","O"],
	["C","O"],
	["C","O","O"],
	["C"],
	["N","N"],
	["C","H"],
	["C","C","H"],
	["C","C","H","H","H"],
	["C","H","H","H","H"], //methane
	["H","C","O"], //formaldehyde
	["H","C","O","O"], //formaldehyde
	["H","C","O","O","H"], //formic acid
	["C","C","C","C","C","C","H","H","H","H","H"], //benzene full
	["C","C","C","H"], //1 part benzene
	["C","C","H","O"], //1 part acetic acid C2H4O2
	["O","H"],
	["H","C","O","O","H"], //formic acid
	["C","H","H","H","Cl"],
	["S","O"],
	["S","O","O"],
	["H","Cl"],
	["C","H","Cl"]
];

~woodplaydfl = {
	var waitTime = 60.0 + (18.0.rand),
	portion = 0.1 + (0.8.rand),
	freq = (10.rand + 3) * 40.0,
	l = ~wood.choose.scramble;
	[waitTime, freq, portion, l].postln;
	~caffplay.(l,
		attack:waitTime*portion, 
		decay:(1.1*waitTime)*(1.0-portion),
		freq:(20.rand + 3)*40.0);
};
~woodplay = {
	|waitTime = -1.0, portion = -1.0, freq = -1.0, l= -1.0|
	waitTime = if(waitTime == -1.0,{60.0 + (18.0.rand)},{waitTime});
	portion = if(portion == -1.0,{0.1 + (0.8.rand)},{portion});
	freq = if(freq == -1.0,{(10.rand + 3) * 40.0},{freq});
	l = if(l == -1.0,{~wood.choose.scramble},{l.scramble});
	[waitTime, freq, portion, l].postln;
	~caffplay.(l,
		attack:waitTime*portion, 
		decay:(1.1*waitTime)*(1.0-portion),
		freq:freq);
};







~woodplaydfl.()
~woodplay.(freq: 80, l: ~wood.choose)
// this is too loud
// was 0.9
~longwood = ~woodplay.(waitTime: 60*17, portion: 0.1, freq: 110, l: ["H","O","O"])
~longwood = ~woodplay.(waitTime: 60*17, portion: 0.1, freq: 440, l: ["H","O","O"])
~longwood = ~woodplay.(waitTime: 20, portion: 0.1, freq: 440, l: ["H","O","O"])
~longwood = ~woodplay.(waitTime: 20, portion: 0.1, freq: -1.0, l: ["H","O","O"])

~longwood = ~woodplay.(waitTime: 120, portion: 0.1, freq: 2**16.linrand, l: ~wood.choose.scramble)


~woodplay.(waitTime: 10+10.linrand, freq: 4000.rand+20, l: ~wood.choose)
~woodplay.(waitTime: 10+40.linrand, freq: 40+2000.rand, l: ~wood.choose
~woodplay.(waitTime: 10+40.linrand, freq: 40+200.linrand, l: ~wood.choose)
~woodplay.(waitTime: 10+40.linrand, freq: 40+200.linrand, l: ["H","Cl"])
~woodplay.(freq: 440, l: ["H","Fe","H","Fe"])
~woodplay.(freq: 20, l: ["H","Fe","H","Fe"])

~woodplay.(waitTime: 120+60.linrand, freq: (1+10.linrand) * 120.0, l: ~wood.choose.scramble)

~woodplay.(waitTime: 10+10.linrand, freq: 10000.rand, l: ["H","Cl"])
~woodplay.(waitTime: 10+120.linrand, freq: 1000.rand, l: ["O","O","Fe","Fe","Fe"])
~woodplay.(waitTime: 10+10.linrand, freq: 10000.rand, l: ~wood.choose.scramble)

~woodplay.(freq: 1680, l: ~wood.choose)


{
	var l = ~wood.choose;
	5.do{|x| 
		~woodplay.(waitTime: 120+60.linrand, freq: (x+1) * 114.0, l: l)
	}
}.()

5.do {
~woodplay.(waitTime: 30.rand, portion: 1.0.rand , freq: 220*(1+10.rand), l: [["Fe","S","Cl","H","C"].choose])
}

5.do {
~woodplay.(waitTime: 600.rand, portion: 1.0.rand , freq: 220*(1+10.rand), l: [["Fe","S","Cl","H","C"].choose])
}


~free = {|synthn|
	s.sendBundle(nil,Synth.basicNew("default",s,synthn).freeMsg)
};




~cool = ();
~buildWindow = {
        var window, cv, updateBtn, synthids, updateButtons, buttons; 
	    var n = 20;
        window = Window("", Rect(200, 200, 200, 400), false).front;
        cv = View(window);
        cv.minSize_(Size(199,399));
        cv.layout_(VLayout());	
        updateBtn = {
           |but, string| but.states = [[string.asString, Color.black, Color.red]];
           but.refresh;
        };
        buttons = n.collect {|i| 
			var btn = Button(cv,Rect(50, 50, 50, 30));
			btn.action_( { Synth.basicNew("",s,synthids[i]).release(7.0); } );
			updateBtn.value(btn,""+i)
		};
        synthids = n.collect { 0 };
        updateButtons = {
			n.do {|i| 
				if(i < ~cool.size, {
					updateBtn.value( buttons[i], ~cool.[i].value);
					synthids[i] = ~cool.[i].key
				}, {
					updateBtn.value( buttons[i], "");
					synthids[i] = 0
				});
			};
		};
	    updateButtons
};
~updater = ~buildWindow.();
~querySynths = {
		var collectChildren, levels, countSize;
		var resp, done = false;
        var updater, updateFunc;
        var interval = 3.0;
        var update;
		resp = OSCFunc({ arg msg;
			var finalEvent;
			var i = 2, j, controls, printControls = false, dumpFunc;
			if(msg[1] != 0, {printControls = true});
			dumpFunc = {|numChildren|
				var event, children;
				event = ().group;
				event.id = msg[i];
				event.instrument = nil; // need to know it's a group
				i = i + 2;
				children = Array.fill(numChildren, {
					var id, child;
					// i = id
					// i + 1 = numChildren
					// i + 2 = def (if synth)
					id = msg[i];
					if(msg[i+1] >=0, {
						child = dumpFunc.value(msg[i+1]);
					}, {
						j = 4;
						child = ().synth.instrument_(msg[i+2]);
						if(printControls, {
							controls = ();
							msg[i+3].do({
								controls[msg[i + j]] = msg[i + j + 1];
								j = j + 2;
							});
							child.controls = controls;
							i = i + 4 + (2 * controls.size);
						}, {i = i + 3 });
					});
					child.id = id;
				});
				event.children = children;
				event;
			};
			finalEvent = dumpFunc.value(msg[3]);
			done = true;
			collectChildren = {|group|
				group.children.collect({|child|
					if(child.children.notNil,{
						child.id -> collectChildren.value(child);
					}, {
						child.id -> child.instrument;
					});
				});
			};
			levels = collectChildren.value(finalEvent);
			countSize = {|array|
				var size = 0;
				array.do({|elem|
					if(elem.value.isArray, { size = size + countSize.value(elem.value) + 2}, {size = size + 1;});
				});
				size
			};
		}, '/g_queryTree.reply', s.addr).fix;        
		updateFunc = {
			fork {
				loop {
					s.sendMsg("/g_queryTree", 0, 0);
					interval.wait;
					~cool = levels[0].(1);
					AppClock.sched(0.0,{
						~updater.();
						nil;
					});
					
				}
			}
		};
		updater = updateFunc.value;
};
~querySynths.();
