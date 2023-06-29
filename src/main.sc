require: slotfilling/slotFilling.sc
  module = sys.zb-common
  
require: patterns.sc
  module = sys.zb-common
require: address/address.sc
  module = sys.zb-common
  
require: patterns.sc
require: stepByStep.sc
theme: /

    state: Start
        q!: $regex</start>
        a: Начнём.

    state: Hello
        q!: (привет/ku)
        a: Привет привет
        go!: /StepByStep/AskRegion
            
    state: Chaotic

    state: NoMatch
        event!: noMatch
        a: Я не понял. Вы сказали: {{$request.query}}
