patterns:
    $streetType = (~улица|~переулок|~проспект|проект|~проезд|~бульвар|шоссе|микрорайон|~площадь|аллеи|аллея|~съезд|~набережная|~канал|километр|тупик)
    $customHouse = {[дом [номер]] $regexp<(\d+(\/\d+)?)+>} $houseFeatures
    $houseFeatures = {[~строение $regexp<(\d|[А-Яа-я])+>] [~корпус $regexp<(\d|[А-Яа-я])+>] [[литер/литера] $regexp<[А-Яа-я]+>]}
    $streetName = $regexp_i<[А-Яа-я1-9]+[А-Яа-я\d-\s]*>