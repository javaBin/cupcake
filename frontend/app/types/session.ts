declare interface Session {
    id: string;
    title: string;
    abstract: string;
    format: string;
    status: string;
    language: string;
    length?: number;
    speakers: Speaker[];
}