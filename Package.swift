// swift-tools-version:5.9
import PackageDescription

let package = Package(
    name: "SolanaKotlin",
    platforms: [
        .iOS(.v16)
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
            url: "https://github.com/avianlabs/solana-kotlin/releases/download/0.4.2/SolanaKotlin.zip",
            checksum: "ed2545ac01d5608adf7892da93c35bebbbafc26f1acd75ef83b9f99f4789c14c"
        ),
    ]
)
