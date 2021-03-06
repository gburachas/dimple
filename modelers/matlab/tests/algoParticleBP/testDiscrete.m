%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%   Copyright 2013 Analog Devices, Inc.
%
%   Licensed under the Apache License, Version 2.0 (the "License");
%   you may not use this file except in compliance with the License.
%   You may obtain a copy of the License at
%
%       http://www.apache.org/licenses/LICENSE-2.0
%
%   Unless required by applicable law or agreed to in writing, software
%   distributed under the License is distributed on an "AS IS" BASIS,
%   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
%   See the License for the specific language governing permissions and
%   limitations under the License.
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

function testDiscrete()
    fg = FactorGraph();
    fg.Solver = 'particleBP';
    b = Bit(3,1);
    fg.addFactor(@xorDelta,b);
    b(1:2).Input = ones(2,1)*.8;
    fg.solve();
    belief1 = b(3).Belief;
    
    fg = FactorGraph();
    fg.Solver = 'SumProduct';
    b = Bit(3,1);
    fg.addFactor(@xorDelta,b);
    b(1:2).Input = ones(2,1)*.8;
    fg.solve();
    belief2 = b(3).Belief;
    assertElementsAlmostEqual(belief1,belief2);
end