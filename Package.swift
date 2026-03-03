// swift-tools-version:5.10
import PackageDescription

let package = Package(
    name: "SolanaKotlin",
    platforms: [
        .iOS(.v17)
    ],
    products: [
        .library(
            name: "SolanaKotlin",
            targets: ["SolanaKotlin"]
        ),
    ],
    targets: [
        .binaryTarget(
            name: "SolanaKotlin",
            url: "https://github.com/avianlabs/solana-kotlin/releases/download/0.4.4/SolanaKotlin.zip",
            checksum: "b0acf819937cbdb8277b2d18b892bf452f19502d039cdac4400f0dc6a4986180"
        ),
    ]
)
