SuperCollider Sound of Elements
===============================

Sounds of the spectra of the Elements.

Based on extracted spectrograms, play the sound of the elements.

This is done by sampling lines of the spectrogram and playing them as
SineOscillators with the lengths scaled.

See elements.R . Code partially stolen from Minute Physics

https://www.youtube.com/watch?v=IhvW8yZdE5A
https://www.youtube.com/watch?v=qyi5SvPlMXc

        // make a Carbon synth
        ~carbon = ~mkelement.(~elem["C"],n:n);
        // play it
        ~cs = ~carbon.play();
        ~cs.set(\freq,440.0);
        ~cs.set(\amp,0.5);
        
Extracted Elements: H,He,Li,B,Be,H,Fe,Rb

Spirit
======

Let's listen to some Elements.

https://www.youtube.com/watch?v=IhvW8yZdE5A
https://www.youtube.com/watch?v=qyi5SvPlMXc

From MinutePhysics Tutorial: creating the sound of hydrogen 

Data Source
===========

@Misc{NIST_ASD,
author = {A.~Kramida and {Yu.~Ralchenko} and 
J.~Reader and {and NIST ASD Team}},
HOWPUBLISHED = {{NIST Atomic Spectra Database 
(ver. 5.1), [Online]. Available:
{\tt{http://physics.nist.gov/asd}} [2014, August 10]. 
National Institute of Standards and Technology, 
Gaithersburg, MD.}},
year = {2013},
}

http://nist.gov/pml/data/asd.cfm

I queried lines in here:

http://physics.nist.gov/PhysRefData/ASD/lines_form.html

200 nm to 2000nm

cm-1 energy units

Lines  "Only with observed wavelengths"

ASCII Text output.

Then I copied into GVim and manipulated these tables into just Observed Wavelength and Ek


-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
Spectrum  |            Observed  |  Rel. |    Aki    | Acc. |       Ei           Ek       |         Lower level          |             Upper level             |Type|                   TP  |   Line  |
          |           Wavelength |  Int. |    s^-1   |      |     (cm-1)       (cm-1)     |------------------------------|-------------------------------------|    |                  Ref. |   Ref.  |
          |            Air (nm)  |       |           |      |                             | Conf.           | Term | J   | Conf.                | Term   | J   |    |                       |         |
-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
          |                      |       |           |      |                             |                 |      |     |                      |        |     |    |                       |         |
C III     |            216.294   |   250 | 8.18e+08  | B    |  276482.86   -   322702.02  | 1s2.2s.3d       | 1D   | 2   | 1s2.2s.4f            | 1F*    | 3   |    |                 T5633 |  L1113  |
C V       |            227.091   |    40 | 5.669e+07 | AA   | 2411271.2    -  2455293.2   | 1s.2s           | 3S   | 1   | 1s.2p                | 3P*    | 2   |    |               T5997LS |  L1113  |
C V       |            227.725   |     5 | 5.621e+07 | AA   | 2411271.2    -  2455169.9   | 1s.2s           | 3S   | 1   | 1s.2p                | 3P*    | 0   |    |               T5997LS |  L1113  |
C V       |            227.792   |    20 | 5.616e+07 | AA   | 2411271.2    -  2455157.3   | 1s.2s           | 3S   | 1   | 1s.2p                | 3P*    | 1   |    |               T5997LS |  L1113  |
C III     |            229.687   |   800 | 1.376e+08 | A+   |  102352.04   -   145876.13  | 1s2.2s.2p       | 1P*  | 1   | 1s2.2p2              | 1D     | 2   |    |                   u24 |  L1113  |

A table like this

------------------------------------
            Observed  |    Ek       
           Wavelength |  (cm-1)     
            Air (nm)  |             
------------------------------------
                      |             
            216.294   |  322702.02  
            227.091   | 2455293.2   
            227.725   | 2455169.9   
            227.792   | 2455157.3   
            229.687   |  145876.13  

Then I ran it through an R script. See elements.R and it produced CSV files.

Rscript elements.R should regen the csv files.

Then in supercollider I made some synths to handle it using a sampling algorithm.


License
=======

Code is under Apache 2.0 Unless otherwise specified.

 Copyright 2014 Abram Hindle
 
 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at
 
     http://www.apache.org/licenses/LICENSE-2.0
 
 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.

Data belongs to a NIST

@Misc{NIST_ASD,
author = {A.~Kramida and {Yu.~Ralchenko} and 
J.~Reader and {and NIST ASD Team}},
HOWPUBLISHED = {{NIST Atomic Spectra Database 
(ver. 5.1), [Online]. Available:
{\tt{http://physics.nist.gov/asd}} [2014, August 10]. 
National Institute of Standards and Technology, 
Gaithersburg, MD.}},
year = {2013},
}
