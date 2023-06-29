patterns:
    $Address = {[$addressCity] * ($addressStreet * $addressHome)}
    $addressHome = $regexp<(((д|Д)(\.?\s|\.?))?|(((д|Д)ом)\s)?)(\d+(-|\/)?[А-Яа-я0-9]*)+> {[$regexp<(((к|К)(орп)?(\.?|\.?\s))?|(((К|к)(орпус|орп\.?))\s)?)\d+(-|\/)?[А-Яа-я0-9]*>] [$regexp<((((л|Л)(ит)?(\.?|\.?\s))|(((Л|л)(итера|итер))\s))[А-Яа-яa-zA-Z0-9]+?)>] [$regexp<(((п|П)(\.?|\.?\s))?|(((П|п)(одъезд|арадная))\s)?)\d+(-|\/)?[А-Яа-я0-9]*>]}
    $addressStreetRegexp = $regexp<\b((У|у)л(\.[А-Яа-я]+|иц.)?|(П|п)ер(-к|еул..|.?)?|(П|п)р(-кт|-т|оспект.?|осп.?|оезд.?|-д.?)?|(Б|б)(-р|ульвар.?|ульв.?)|(Ш|ш)(оссе|.?)|(М|м)(икрорайон|-н|кр|рн|крн|ийон|икр-он)|(П|п)(лощадь|л.?|-дь)|(А|а)(л.?|-я|ллея|лея)|(С|с)(ъезд|-д)|(Н|н)аб(.?|ережная)|(О|о)(строва|-ва)|(Д|д)(орога|ор|-а)|(К|к)(анал|-л))>
    $addressWordsRegexp = $regexp<[А-Яа-я0-9]+(((\-|\s)([А-Яа-я]+|\w))?((\-|\s)(\w|[А-Яа-я]+))?)?>
    $addressWord = $regexp<([А-Яа-я]+|\w)[^(дом|Дом|д\.|д)]>
    
    $addressStreet = ($addressStreetRegexp $addressWordsRegexp [$addressWord]
        |$regexp<((У|у)л|(П|п)(ер|р|л)|(Ш|ш)|(Б|б)ульв|(М|м)(кр.?)|(Н|н)аб)\.[А-Яа-я0-9]+(((\-|\s)(\w|[А-Яа-я]+))?((\-|\s)(\w|[А-Яа-я]+))?)?+>
        |$addressWordsRegexp $regexp<\b((Л|л)(иния|.?|-я)|(П|п)р(-кт|-т|оспект.?))> [$regexp<(((В|в)(\.?|\s?)?(О|о)\.?|(П|п)(\.?|\s?)(С|с)\.?|(В|в)асильевского (О|о)(строва|-ва))|(П|п)етроградской (С|с)тороны)?>]
        |$addressWordsRegexp [$addressWord] $addressStreetRegexp
        |$addressWordsRegexp [$addressWord] $regexp<((у)л\.\,?)>
        |(МКАД|КАД) {($Number|$regexp<\d+-?й>) км})
    $addressCity = ($regexp<\b(Г|г)(ор|ород.?)?> $regexp<^[А-Яа-я]+>|$regexp<^(Г|г)\.[А-Яа-я]+>)

theme: /
    
    
