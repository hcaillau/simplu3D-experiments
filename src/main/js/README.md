# Intégration NodeJS

## Utilisation

```bash
npm install --save git+https://github.com/hcaillau/simplu3D-experiments.git#master
```

```js
const simplu3d = require('simplu3d-experiments');

simplu3d.run(
    'fr.ign.cogit.simplu3d.experiments.smartplu.data.DataPreparator',[
        'data/municipality/74042/parcelle.json',
        'data/municipality/74042/building'
    ]
);
```