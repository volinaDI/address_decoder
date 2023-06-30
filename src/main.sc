require: slotfilling/slotFilling.sc
  module = sys.zb-common
  
require: patterns.sc
  module = sys.zb-common
require: address/address.sc
  module = sys.zb-common
  
require: patterns.sc
require: stepByStep.sc

require: functions.js
theme: /

    state: Start
        q!: $regex</start>
        a: Здравствуйте, попробуете назвать адрес в хаотичном порядке?
        q: * $yes * || toState = "/SuperChaotic"
        q: * $no * || toState = "/StepByStep"
            

    state: Hello
        q!: (привет/ku)
        a: Привет привет
        go!: /StepByStep/AskRegion
            
    state: SuperChaotic
        q!: (арбуз/ch/ср)
        a: Какой адрес вас интересует?
        
        state: GetAddress
            q: *
            a: {{toPrettyString(getFullAddress($request.query))}}

    state: NoMatch
        event!: noMatch
        a: Я не понял. Вы сказали: {{$request.query}}

    state: TMP
        q!: tmp
        a: {{$injector.dadata.url}}