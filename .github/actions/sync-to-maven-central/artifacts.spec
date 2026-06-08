{
  "files": [
    {
      "aql": {
        "items.find": {
          "$and": [
            {
              "@build.name": "${buildName}",
              "@build.number": "${buildNumber}",
              "path": {
                "$match": "org/springframework*"
              }
            },
            {
              "$or": [
                {
                  "name": {
                    "$match": "*.pom"
                  }
                },
                {
                  "name": {
                    "$match": "*.jar"
                  }
                },
                {
                  "name": {
                    "$match": "*.module"
                  }
                },
                {
                  "name": {
                    "$match": "*.asc"
                  }
                }
              ]
            },
            {
              "name": {
                "$nmatch": "*.zip.asc"
              }
            }
          ]
        }
      },
      "target": "nexus/"
    }
  ]
}
